package com.aurora.dto.user;

import com.aurora.entity.enums.Language;
import com.aurora.entity.enums.Theme;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String nome;
    private String email;
    private String fotoUrl;
    private String uf;
    private String cidade;
    private String endLogradouro;
    private String endNumero;
    private String endComplemento;
    private String endBairro;
    private String endCidade;
    private String endUf;
    private String endCep;
    private String cartaoNumero;
    private Theme tema;
    private Language idioma;
}
