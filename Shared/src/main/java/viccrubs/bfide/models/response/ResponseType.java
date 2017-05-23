package viccrubs.bfide.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by viccrubs on 2017/5/11.
 */
public enum ResponseType {
    @SerializedName("LOGIN_RESPONSE")
    LoginResponse,
    @SerializedName("REGISTER_RESPONSE")
    RegisterResponse,
    @SerializedName("RUN_RESULT")
    RunResult,
    @SerializedName("REQUEST_INPUT")
    RequestInput,
    @SerializedName("TEST_CONNECTION")
    TestConnection
}