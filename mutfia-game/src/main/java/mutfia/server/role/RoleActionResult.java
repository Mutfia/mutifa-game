package mutfia.server.role;

public record RoleActionResult(boolean success, String message) {
    public static RoleActionResult success(String message) {
        return new RoleActionResult(true, message);
    }

    public static RoleActionResult failure(String message) {
        return new RoleActionResult(false, message);
    }
}
