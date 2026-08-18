package com.jqwik.demo;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import static org.assertj.core.api.Assertions.assertThat;

class EmailMaskerPropertyTest {

    // Propriedade 1: o domínio do e-mail mascarado deve ser igual ao domínio original.
    // Esse teste valida a regra de negócio principal de preservação de domínio.
    // - - maskedEmailShouldPreserveDomain: valida que o e-mail mascarado mantém exatamente o mesmo domínio após o @. Ou seja, só a parte local pode mudar, o domínio não.
    @Property
    void maskedEmailShouldPreserveDomain(@ForAll("emails") String email) {
        String masked = EmailMasker.mask(email);

        String[] originalParts = email.split("@", 2);
        String[] maskedParts = masked.split("@", 2);

        assertThat(maskedParts).hasSize(2);
        assertThat(maskedParts[1]).isEqualTo(originalParts[1]);
    }

    // Propriedade 2: a parte local deve manter o primeiro e último caractere,
    // e os caracteres intermediários devem ser asteriscos.
    // - - maskedEmailShouldHideLocalPartCharacters: valida a parte local do e-mail. Ele espera que:o primeiro caractere continue visível, o último caractere continue visível, os caracteres entre eles viram * (ou, se o nome for muito curto, usa apenas um *).
    @Property
    void maskedEmailShouldHideLocalPartCharacters(@ForAll("emailLocalParts") String localPart,
                                                  @ForAll("emailDomains") String domain) {
        String email = localPart + "@" + domain;
        String masked = EmailMasker.mask(email);

        String maskedLocal = masked.split("@", 2)[0];

        assertThat(maskedLocal.length()).isEqualTo(localPart.length());
        assertThat(maskedLocal.charAt(0)).isEqualTo(localPart.charAt(0));

        if (localPart.length() <= 2) {
            // Para nomes de usuário muito curtos, a máscara usa apenas um asterisco.
            assertThat(maskedLocal).isEqualTo(localPart.charAt(0) + "*");
        } else {
            assertThat(maskedLocal.charAt(maskedLocal.length() - 1)).isEqualTo(localPart.charAt(localPart.length() - 1));
            assertThat(maskedLocal.substring(1, maskedLocal.length() - 1)).matches("\\*+");
        }
    }

    // Gerador de e-mails válidos para uso nos testes de propriedade.
    // O método cria a parte local e combina com diferentes domínios.
    @Provide
    Arbitrary<String> emails() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(10)
                .flatMap(local -> Arbitraries.of("example.com", "test.org", "domain.net", "sample.co")
                        .map(domain -> local + "@" + domain));
    }

    // Gerador específico para a parte local do e-mail.
    // Esse arbitrary produz nomes de usuário variados para testar o mascaramento.
    @Provide
    Arbitrary<String> emailLocalParts() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(10);
    }

    // Lista de domínios que serão usados no teste.
    // Mantém o foco na lógica de mascaramento, sem depender de domínios reais variados.
    @Provide
    Arbitrary<String> emailDomains() {
        return Arbitraries.of("example.com", "test.org", "domain.net", "sample.co");
    }
}
