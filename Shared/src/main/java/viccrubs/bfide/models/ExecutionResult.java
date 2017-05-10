package viccrubs.bfide.models;

import com.google.gson.annotations.Expose;
import viccrubs.bfide.bfmachine.exceptions.BFMachineException;

/**
 * Created by viccrubs on 2017/5/8.
 */
public class ExecutionResult {
    @Expose
    public final BFMachineException exception;
    @Expose
    public final String output;

    public ExecutionResult(BFMachineException exception, String output) {
        this.exception = exception;
        this.output = output;
    }
}