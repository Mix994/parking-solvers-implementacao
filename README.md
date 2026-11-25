# SmartPark — Implementação Android (`Parking Solvers`)

Aplicativo Android nativo (**Kotlin**, **Jetpack Compose**, **Retrofit 2**, **OkHttp 4** e **JWT**) do sistema **SmartPark**, vinculado como submódulo `3.Implementacao` do repositório principal [parking-solvers](https://github.com/Mix994/parking-solvers).

> **Sobre esta branch (`feat/api-spring-boot`):** o banco local SQLite/Room foi removido e substituído por comunicação direta via API REST com o servidor Spring Boot (`3.Servidor-API`). O app opera em modo **online-only**. A branch `main` mantém a versão anterior com Room.

---

## Tecnologias Utilizadas, Como e Onde Foram Aplicadas

| Tecnologia | Como foi utilizada | Onde está no projeto |
| :--- | :--- | :--- |
| **Kotlin 2.2** | Linguagem principal do aplicativo, utilizando coroutines, `Flow`, `StateFlow` e data classes para DTOs. | Todo o código-fonte em `app/src/main/java/com/parkingsolvers/smartpark/` |
| **Jetpack Compose + Material 3** | Construção declarativa de todas as telas (Login, Vagas Disponíveis, Dashboard, Portaria, Visitantes, Veículos, Gestão) e componentes visuais (`AppShell`, `PlacaMercosulChip`, `SmartParkCard`, `MeusDadosDialog`). | `**/ui/*Screen.kt`, `ui/components/` e `ui/theme/` |
| **Navigation Compose + ViewModel** | Navegação dinâmica por perfil (`ADMIN`, `PORTARIA`, `SERVIDOR`) e gerenciamento de estado das telas com `ViewModel` e `StateFlow`. | `ui/navigation/SmartParkRoutes.kt`, `ui/components/AppShell.kt` e `**/ui/*ViewModel.kt` |
| **Retrofit 2 + Gson** | Definição da interface REST com todos os endpoints do servidor Spring Boot e serialização/desserialização JSON. | `rede/SmartParkApi.kt`, `rede/ApiDtos.kt`, `rede/ApiClient.kt` |
| **OkHttp 4 (`AuthInterceptor`, `TokenAuthenticator`, `HttpLoggingInterceptor`)** | Anexa o header `Authorization: Bearer <accessToken>` em todas as chamadas autenticadas, renova automaticamente tokens expirados (`401`) via `POST /auth/refresh` de forma thread-safe e redireciona para o login se o refresh falhar. | `rede/ApiClient.kt` |
| **SharedPreferences (`SessaoStore`)** | Armazena `accessToken`, `refreshToken` e dados do `UsuarioSessao` para manter o usuário autenticado entre aberturas do app (com implementação em memória `MemoriaSessaoStore` para testes). | `rede/SessaoStore.kt` |
| **Mapeamento de Erros HTTP (`ApiErrorMapper`)** | Converte respostas de erro da API (`400`, `401`, `403`, `404`, `409`, `422`, `429`) e falhas de rede/timeout (`IOException`, `SocketTimeoutException`) de volta nas exceções de domínio usadas pela UI (`ValidacaoException`, `ConflitoException`, `CredenciaisInvalidasException`, `BloqueioTentativasException`, `RegraDeNegocioException`). | `rede/ApiErrorMapper.kt` e `compartilhado/exceptions/` |
| **Conversor ISO-8601 (`Iso8601`)** | Converte datas `Instant` ISO-8601 UTC retornadas pela API para epoch millis (e vice-versa) sem depender de `java.time` da API 26+, mantendo compatibilidade com `minSdk 24`. | `rede/Iso8601.kt` |
| **Sincronização Reativa (`SincronizadorDados` + `fluxoRemoto`)** | Substitui os `Flow` reativos do antigo Room: recarrega automaticamente listas e dashboard após qualquer operação de escrita ou clique no botão **Atualizar** na barra superior. | `rede/FluxoRemoto.kt`, `rede/ApiClient.kt` e `**/service/*Service.kt` |
| **Network Security Config + `BuildConfig`** | Exige **HTTPS** em builds `release` (`cleartextTrafficPermitted="false"`) e libera HTTP apenas em `debug` para desenvolvimento local; injeta a URL da API em tempo de build via `BuildConfig.API_BASE_URL`. | `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/network_security_config.xml`, `app/src/debug/res/xml/network_security_config.xml`, `app/build.gradle.kts` |
| **JUnit 4 + OkHttp MockWebServer + Coroutines Test** | Testa o cliente HTTP completo simulando respostas reais da API (login, Bearer, refresh automático em 401, tradução de erros HTTP), além do conversor ISO-8601 e validadores de placa/formulário. | `app/src/test/java/com/parkingsolvers/smartpark/rede/` e `compartilhado/validacao/` |

---

## Arquitetura por Domínio

```text
com.parkingsolvers.smartpark
├── SmartParkApplication.kt   # AppContainer (injeção manual de ApiClient, SessaoStore, SincronizadorDados e Services)
├── MainActivity.kt           # Alterna entre LoginScreen e AppShell conforme AuthService.sessaoAtual
├── rede/                     # SmartParkApi (Retrofit), ApiClient (OkHttp + JWT/Refresh), ApiDtos, ApiErrorMapper, SessaoStore, Iso8601, FluxoRemoto
├── auth/                     # LoginDTO, PerfilUsuario, UsuarioSessao, AuthService (JWT), LoginViewModel, LoginScreen
├── usuario/                  # DTOs, UsuarioService (/usuarios e /usuarios/me), UsuarioViewModel, UsuariosScreen
├── servidor/                 # DTOs, ServidorService (/servidores), ServidorViewModel, ServidoresScreen
├── veiculo/                  # DTOs, TipoCadastroVeiculo, VeiculoService (/veiculos e consulta de placa), VeiculoViewModel, VeiculosScreen
├── movimentacao/             # DTOs, TipoVaga, Turno, MovimentacaoService (/movimentacoes), PortariaViewModel, PortariaScreen
├── visitante/                # DTOs, VisitanteService (/visitantes), VisitanteViewModel, VisitantesScreen
├── dashboard/                # ConfiguracaoEstacionamento, PeriodoDashboard, DashboardService, ConfiguracaoEstacionamentoService, DashboardScreen, VagasDisponiveisScreen
├── compartilhado/            # Exceções de domínio, ValidadorPlaca, ValidadorFormulario, Formatadores, TurnoUtils
└── ui/                       # Tema institucional, PlacaMercosulChip, SmartParkCard, AppShell (com botão Atualizar), MeusDadosDialog
```

---

## Como Conectar à API, Compilar e Testar

### 1. Subir a API localmente
Primeiro garanta que o PostgreSQL e o servidor Spring Boot (`../3.Servidor-API`) estejam rodando em `http://localhost:8080`.

### 2. Conectar o emulador ou celular físico
Em modo `debug`, a URL padrão é `http://127.0.0.1:8080/`. Antes de abrir o app no emulador Android (especialmente **Android 17 / API 37**, que bloqueia IPs de rede privada como `10.0.2.2` pelo *Local Network Protection*) ou em celular conectado via USB, execute:

```powershell
adb reverse tcp:8080 tcp:8080
```

Para apontar para outro endereço (ex.: servidor em rede local ou produção HTTPS), sobrescreva a propriedade `API_BASE_URL`:

```powershell
# Debug apontando para outro IP
.\gradlew.bat installDebug -PAPI_BASE_URL=http://192.168.1.50:8080/

# Release (apenas HTTPS é aceito pelo network_security_config)
.\gradlew.bat assembleRelease -PAPI_BASE_URL=https://sua-api.seudominio.com/
```

### 3. Compilar e rodar testes unitários

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

O APK debug é gerado em `app/build/outputs/apk/debug/app-debug.apk`.
