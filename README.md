# SmartPark — Implementação Android (`Parking Solvers`)

Código-fonte Android nativo (**Kotlin**, **Jetpack Compose**, **Room v4** e **JUnit 4**) do sistema **SmartPark**, vinculado como submódulo `3.Implementacao` do repositório principal [parking-solvers](https://github.com/Mix994/parking-solvers).

## Onde Localizar o APK Executável

- **Pronto para download no repositório principal:** [`5.Implantação/SmartPark-v1.0.apk`](https://github.com/Mix994/parking-solvers/blob/main/5.Implanta%C3%A7%C3%A3o/SmartPark-v1.0.apk)
- **Gerado localmente após `.\gradlew.bat assembleDebug`:** `app/build/outputs/apk/debug/app-debug.apk`

## Arquitetura por Domínio

```text
com.parkingsolvers.smartpark
├── auth/             # LoginDTO, AuthService, LoginAttemptService (5 falhas / 15 min), SenhaHasher, LoginScreen
├── usuario/          # Entidade Usuario (Room), UsuarioDao, UsuarioService, UsuariosScreen ("Meus dados" + níveis)
├── servidor/         # Entidade Servidor, ServidorService, ServidoresScreen
├── veiculo/          # Entidade Veiculo (Permanente/Temporário), VeiculoService, VeiculosScreen
├── movimentacao/     # Entidade RegistroMovimentacao, TipoVaga (incl. Carregamento Elétrico), MovimentacaoService, PortariaScreen
├── visitante/        # Entidade Visitante (externos), VisitanteService, VisitantesScreen
├── dashboard/        # ConfiguracaoEstacionamento (vagas elétricas expansíveis), PeriodoDashboard, CalculadoraOcupacao, DashboardScreen
├── compartilhado/    # Exceções de domínio, ValidadorPlaca, ValidadorFormulario, RelogioSistema, Formatadores
├── database/         # SmartParkDatabase (Room v4) e Converters
├── dev/              # DadosIniciais (carga automática de demonstração)
└── ui/               # Tema institucional, PlacaMercosulChip, SmartParkCard, AppShell, MeusDadosDialog
```

## Compilar e Rodar Testes

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
