package com.system.inventorysystem.dto;
import lombok.AllArgsConstructor;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class ErrorResponse<T> implements Serializable {
    private int statusCode;
    private String error;
    private Object message;
    private T data;
}