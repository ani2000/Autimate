package org.ww.wigglew.entity.aex;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class QuestionExamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    private List<Integer> questionAnswers;

    public QuestionExamEntity() {}

    public QuestionExamEntity(List<Integer> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuestionAnswers(List<Integer> questionAnswers) {
        this.questionAnswers = questionAnswers;
    }
}
