package org.example.exception;

/**
 * Exceção lançada quando dados de entrada ou regras de negócio são violados.
 */
public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
