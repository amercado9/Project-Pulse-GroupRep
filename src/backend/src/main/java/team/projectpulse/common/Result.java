package team.projectpulse.common;

public record Result<T>(boolean flag, int code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(true, 200, "Success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, 200, message, data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(false, code, message, null);
    }

    public static <T> Result<T> notFound(String message) {
        return new Result<>(false, 404, message, null);
    }
}
