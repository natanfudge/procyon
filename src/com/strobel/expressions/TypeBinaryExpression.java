package com.strobel.expressions;

import com.strobel.reflection.Type;
import com.strobel.util.TypeUtils;

/**
 * @author Mike Strobel
 */
public final class TypeBinaryExpression extends Expression {
    private final Expression _operand;
    private final Type _typeOperand;
    private final ExpressionType _nodeType;

    TypeBinaryExpression(final Expression operand, final Type typeOperand, final ExpressionType nodeType) {
        _operand = operand;
        _typeOperand = typeOperand;
        _nodeType = nodeType;
    }

    public final Expression getOperand() {
        return _operand;
    }

    public final Type getTypeOperand() {
        return _typeOperand;
    }

    @Override
    public final ExpressionType getNodeType() {
        return _nodeType;
    }

    @Override
    public final Type getType() {
        return _typeOperand;
    }

    @Override
    public final boolean canReduce() {
        return _nodeType == ExpressionType.TypeEqual;
    }

    @Override
    public final Expression reduce() {
        if (_nodeType == ExpressionType.TypeEqual) {
            return reduceTypeEqual();
        }
        return this;
    }

    private Expression reduceConstantTypeEqual() {
        final Object constantValue = ((ConstantExpression)_operand).getValue();
        if (constantValue == null) {
            return constant(false);
        }
        return constant(_typeOperand.getErasedClass().isInstance(constantValue));
    }

    private Expression reduceParameterTypeEqual(final ParameterExpression value) {
        final Expression getClass = call(value, Type.Object.getMethod("getClass"));

        // We use reference equality when comparing to null for correctness, and reference equality
        // on types for performance.
        return andAlso(
            referenceNotEqual(value, constant(null)),
            referenceEqual(
                getClass,
                constant(_typeOperand.getErasedClass(), Type.of(Class.class))
            )
        );
    }

    final Expression reduceTypeEqual() {
        final Type cType = _operand.getType();

        // We can evaluate the result right now for constants.
        if (_operand.getNodeType() == ExpressionType.Constant) {
            return reduceConstantTypeEqual();
        }

        if (cType.isPrimitive()) {
            return Expression.block(
                // We still have to emit the operand in case there's a side effect.
                _operand,
                constant(TypeUtils.hasIdentityPrimitiveOrBoxingConversion(cType, _typeOperand))
            );
        }

        // If the operand type is a final reference type, it will match if value is not null.
        if (cType.isFinal() && TypeUtils.areEquivalent(cType, _typeOperand)) {
            return referenceNotEqual(
                _operand,
                constant(null, cType)
            );
        }

        if (_operand instanceof ParameterExpression) {
            return reduceParameterTypeEqual((ParameterExpression)_operand);
        }

        // Create a temporary variable so we only evaluate the left side once.
        final ParameterExpression parameter = variable(Type.Object);

        Expression operand = _operand;

        // Convert to Object if necessary.
        if (!TypeUtils.areReferenceAssignable(Type.Object, cType)) {
            operand = convert(operand, Type.Object);
        }

        return block(
            new ParameterExpressionList(parameter),
            assign(parameter, operand),
            reduceParameterTypeEqual(parameter)
        );
    }

    @Override
    protected final Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitTypeBinary(this);
    }

    public final TypeBinaryExpression update(final Expression operand) {
        if (operand == getOperand()) {
            return this;
        }
        if (getNodeType() == ExpressionType.InstanceOf) {
            return instanceOf(operand, getTypeOperand());
        }
        return typeEqual(operand, getTypeOperand());
    }
}