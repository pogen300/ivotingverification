package ee.vvk.ivotingverification.tasks;

public class Result<T> {
    public final T value;
    public final String errorMessage;

    public Result(T value, String errorMessage) {
        this.value = value;
        this.errorMessage = errorMessage;
    }

    public boolean isFailure() {
        return value == null;
    }
}
