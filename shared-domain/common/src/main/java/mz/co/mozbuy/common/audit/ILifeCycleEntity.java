package mz.co.mozbuy.common.audit;

public interface ILifeCycleEntity<U> {

    boolean isActive();      // retorna true se estiver ativo
    int getState();          // retorna o código do estado (por ex: 0 = ativo, 1 = inativo)
    void activate();         // define estado ativo
    void inactivate();       // define estado inativo
    void delete();           // define estado deletado
    void block();            // define estado bloqueado
    void ban();              // define estado banido
}
