package com.jjang051.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {
    @Id
    @GeneratedValue(strategy =
            GenerationType.SEQUENCE)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> memberList =
            new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
