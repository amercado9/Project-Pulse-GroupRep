package team.projectpulse.system;

public record Result<T>(boolean flag, int code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(true, StatusCode.SUCCESS, "Success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, StatusCode.SUCCESS, message, data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(false, code, message, null);
    }

    public static <T> Result<T> notFound(String message) {
        return new Result<>(false, StatusCode.NOT_FOUND, message, null);
    }

    public static <T> Result<T> conflict(String message) {
        return new Result<>(false, StatusCode.CONFLICT, message, null);
    }
}
