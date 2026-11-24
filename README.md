# SmartPark — Implementação Android (`Parking Solvers`)

Código-fonte Android nativo (**Kotlin**, **Jetpack Compose**, **Room v3** e **JUnit 4**) do sistema **SmartPark**, vinculado como submódulo `3.Implementacao` do repositório principal [parking-solvers](https://github.com/Mix994/parking-solvers).

## Arquitetura por Domínio

```text
com.parkingsolvers.smartpark
├── auth/             # LoginDTO, AuthService, LoginAttemptService (5 falhas / 15 min), SenhaHasher, LoginScreen
├── usuario/          # Entidade Usuario (Room), UsuarioDao, UsuarioService, UsuariosScreen ("Meus dados" + níveis)
├── servidor/         # Entidade Servidor, ServidorService, ServidoresScreen
├── veiculo/          # Entidade Veiculo (Permanente/Temporário), VeiculoService, VeiculosScreen
├── movimentacao/     # Entidade RegistroMovimentacao, MovimentacaoService, PortariaScreen
├── visitante/        # Entidade Visitante (externos), VisitanteService, VisitantesScreen
├── dashboard/        # PeriodoDashboard, CalculadoraOcupacao, DashboardService, DashboardScreen
├── compartilhado/    # Exceções de domínio, ValidadorPlaca, ValidadorFormulario, RelogioSistema, Formatadores
├── database/         # SmartParkDatabase (Room v3) e Converters
├── dev/              # DadosIniciais (carga automática de demonstração)
└── ui/               # Tema institucional, PlacaMercosulChip, SmartParkCard, AppShell, MeusDadosDialog
```

## Compilar e Rodar Testes

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
