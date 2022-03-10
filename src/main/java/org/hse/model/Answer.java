package org.hse.model;

import javax.persistence.*;

@Entity
@Table(name = "answer_table")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String message;

    @Column
    private String user;

    @OneToOne
    private Question question;

    public Answer() {}

    public Answer(String message, String user, Question question) {
        this.message = message;
        this.user = user;
        this.question= question;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
