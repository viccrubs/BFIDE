package viccrubs.bfide.bfmachine.exception;

import viccrubs.bfide.bfmachine.BFMachineStates;

/**
 * Created by viccrubs on 2017/5/30.
 */
public class InputTooShortException extends BFMachineException {
    public InputTooShortException(BFMachineStates states) {
        super(states);
    }
    @Override
    public String description(){
        return "Input is not sufficient. Input more to satisfy codes' requirement.";
    }
}
