one sig InputVector {
	input_4:Int,
	input_5:Int
}one sig parameterVector {
	parameter_value_int_0:Int
}
abstract sig fr_inria_calculator_Calculator {
		currentValue:Int
}one sig fr_inria_calculator_Calculator_0_1 extends fr_inria_calculator_Calculator{}
one sig fr_inria_calculator_Calculator_0_2 extends fr_inria_calculator_Calculator{}
fact {
	parameterVector.parameter_value_int_0 = InputVector.input_4
	fr_inria_calculator_Calculator_0_1.currentValue = parameterVector.parameter_value_int_0
	parameterVector.parameter_value_int_0 = InputVector.input_5
	not rem[fr_inria_calculator_Calculator_0_1.currentValue,3]=0
	fr_inria_calculator_Calculator_0_2.currentValue = plus[fr_inria_calculator_Calculator_0_1.currentValue,mul[2,parameterVector.parameter_value_int_0]]
}
run {} for 2