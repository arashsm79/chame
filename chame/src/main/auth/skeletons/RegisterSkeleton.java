package main.auth.skeletons;

public class RegisterSkeleton {
    private String username;
    private String password;
    private String question;
    private String answer;
    private String email;

    public RegisterSkeleton(String username, String password, String question, String answer, String email) {
        this.username = username;
        this.password = password;
        this.question = question;
        this.answer = answer;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
