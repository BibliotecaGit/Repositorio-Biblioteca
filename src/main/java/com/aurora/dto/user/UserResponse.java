package com.aurora.dto.user;

import com.aurora.entity.enums.Language;
import com.aurora.entity.enums.Theme;
import com.aurora.entity.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String nome;
    private String email;
    private UserType tipo;
    private String fotoUrl;
    private Language idioma;
    private Theme tema;
    private String uf;
    private String cidade;
    private String endLogradouro;
    private String endNumero;
    private String endComplemento;
    private String endBairro;
    private String endCidade;
    private String endUf;
    private String endCep;
    private String cartaoMascarado;
    private LocalDate bloqueadoAte;
}
