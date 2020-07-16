package main.auth.skeletons;

public class ForgotPasswordSkeleton {
    private String email;
    private String recoveryQuestion;
    private String answer;

    public ForgotPasswordSkeleton(String email, String recoveryQuestion, String answer) {
        this.email = email;
        this.recoveryQuestion = recoveryQuestion;
        this.answer = answer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRecoveryQuestion() {
        return recoveryQuestion;
    }

    public void setRecoveryQuestion(String recoveryQuestion) {
        this.recoveryQuestion = recoveryQuestion;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
