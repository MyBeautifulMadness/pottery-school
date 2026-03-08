package com.example.PotteryPotSchool.dto.Posts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paged<T> {
    private List<T> items;
    private int page;
    private int size;
    private long total;
}
