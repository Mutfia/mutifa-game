package mutfia.global.response.enums;

public enum Status {
    OK("요청이 성공적으로 처리되었습니다."),
    ERROR("서버 내부에 오류가 발생했습니다.");

    private final String message;

    Status(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
