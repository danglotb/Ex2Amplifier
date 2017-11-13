one sig InputVector {
	input_0:Int,
	input_1:Int
}
one sig parameterVector {
	parameter_value_int_0:Int
}
abstract sig fr_inria_calculator_Calculator{
		currentValue:Int
}
one sig fr_inria_calculator_Calculator_0_1 extends fr_inria_calculator_Calculator{}
one sig fr_inria_calculator_Calculator_0_2 extends fr_inria_calculator_Calculator{}
one sig fr_inria_calculator_Calculator_0_3 extends fr_inria_calculator_Calculator{}
fact {
	parameterVector.parameter_value_int_0 = InputVector.input_0
	fr_inria_calculator_Calculator_0_1.currentValue = parameterVector.parameter_value_int_0
	parameterVector.parameter_value_int_0 = InputVector.input_1
	rem[fr_inria_calculator_Calculator_0_1.currentValue,3]=0
	fr_inria_calculator_Calculator_0_2.currentValue = plus[fr_inria_calculator_Calculator_0_1.currentValue,parameterVector.parameter_value_int_0]
	rem[fr_inria_calculator_Calculator_0_2.currentValue,5]=0
	not rem[fr_inria_calculator_Calculator_0_2.currentValue,2]=0
}
run {} for 2