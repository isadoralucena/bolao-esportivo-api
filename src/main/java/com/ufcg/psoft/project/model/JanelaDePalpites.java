public record JanelaDePalpites(Integer minutosAbertura, Integer minutosFechamento) {
    public JanelaDePalpites {
        if (minutosAbertura <= minutosFechamento) {
            throw new RegraDeTempoInvalidaException();
        }
    }
}