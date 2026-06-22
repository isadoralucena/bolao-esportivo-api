public class RegrasPalpitesRequestDTO {
    @NotNull @Min(0)
    private Integer minutosAbertura;

    @NotNull @Min(0)
    private Integer minutosFechamento;
}