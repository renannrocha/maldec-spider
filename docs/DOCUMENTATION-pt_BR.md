# Documentação Geral Spider

> [!IMPORTANT]
> A documentação do projeto segue em desenvolvimento, mais atualizações serão disponibilizadas em breve.

[//]: # (Este documento fornece uma visão geral das funcionalidades e dos componentes que constituem a API para o Website da MalDec Labs.)

### **Sumário:**

### [**Introdução**](#Introdução)
- [Descrição geral do projeto.](#Descrição-geral-do-projeto)

### [**Documentação de Requisitos**](#Documentação-de-Requisitos)
- [Requisitos funcionais e não funcionais](#Requisitos-funcionais-e-não-funcionais)
- [Casos de uso e fluxos de trabalho](#Casos-de-uso-e-fluxos-de-trabalho)

### [**Modelagem de Dados**](#Modelagem-de-Dados)
- [Diagramas de modelos](#Diagramas-de-modelos)
- [Descrição das tabelas](#Descrição-das-tabelas)

### [**Arquitetura da API**](#Arquitetura-da-API)
- [Visão geral da arquitetura da API](#Visão-geral-da-arquitetura-da-API)
- [Componentes principais](#Componentes-principais)

### [**Endpoints e Rotas**](#Endpoints-e-Rotas)
- [Lista de endpoints disponíveis na API](#Lista-de-endpoints-disponíveis-na-API)
- [Descrição de cada rota](#Descrição-de-cada-rota)

### [**Autenticação e Autorização**](#Autenticação-e-Autorização)
- [Como os usuários autenticam-se na API](#Como-os-usuários-autenticam-se-na-API) 
- [Políticas de autorização e permissões](#Políticas-de-autorização-e-permissões)

### [**Testes e Validação**](#Testes-e-Validação)
- [Estratégias de testes](#Estratégias-de-testes)
- [Exemplos de testes e validação](#Exemplos-de-testes-e-validação)

### [**Boas Práticas e Padrões**](#Boas-Práticas-e-Padrões)
- [Convenções de nomenclatura](#Convenções-de-nomenclatura) 
- [Padrões de codificação e estilo](#Padrões-de-codificação-e-estilo)

### [**Referências Externas**](#Referências-Externas)
- [Links para documentos relacionados](#Links-para-documentos-relacionados)
- [Referências a bibliotecas ou frameworks utilizados](#Referências-a-bibliotecas-ou-frameworks-utilizados) 

### [**Monitoramento e Logs**](#Monitoramento-e-Logs)
- [Como os logs são gerados e armazenados](#Como-os-logs-são-gerados-e-armazenados)
- [Descrição dos mecanismos de monitoramento da API](#Descrição-dos-mecanismos-de-monitoramento-da-API)

### [**Segurança**](#Segurança)
- [Práticas de segurança recomendadas](#Práticas-de-segurança-recomendadas)
- [Políticas de segurança](#Políticas-de-segurança)

### [**Escalabilidade e Desempenho**](#Escalabilidade-e-Desempenho)
- [Estratégias para dimensionamento horizontal ou vertical](#Estratégias-para-dimensionamento-horizontal-ou-vertical)
- [Otimização de consultas de banco de dados e cache](#Otimização-de-consultas-de-banco-de-dados-e-cache)

[**Licença e Direitos Autorais**](#Licença-e-Direitos-Autorais)

<hr>


# Introdução
O Website MalDec Labs é uma plataforma online especializada em análise de malware. Seu principal objetivo é realizar análise de arquivos suspeitos enviados pelos usuários e determinar se contêm ameaças.

<picture>
    <img alt="home page MalDec Labs" src="./assets/bannerHomePage.png">
</picture>

Através do MalDec Engine (uma inteligência artificial desenvolvida pela MalDec Labs), ele analisa esses arquivos e fornece informações detalhadas sobre sua natureza e seu potencial risco. Além disso, a plataforma oferece opções de planos de assinatura e um Marketplace para compra e venda de análises. Em resumo, o MalDec é uma ferramenta essencial para a segurança cibernética, ajudando a proteger sistemas e redes contra ameaças digitais.

## Descrição geral do projeto
O WebSite-API é uma aplicação RESTful que é responsável por todos os serviços de back-end do website da MalDec Labs. Através da interface do WebSite-API, é possivel realizar requisições seguindo todos parâmetros necessários para executar um conjunto de operações que estarão descritas nessa documentação.

<hr>

# Documentação de Requisitos

## Requisitos funcionais e não funcionais

## Casos de uso e fluxos de trabalho

<hr>

# Modelagem de Dados

## Diagramas de modelos

## Descrição das tabelas

<hr>

# Arquitetura da API

## Visão geral da arquitetura da API

````shell
src/main/java/org/maldeclabs/spider

├── java/
│   ├── org.maldeclabs.spider/
│   │   ├── application/
│   │   │   ├── services/
│   │   │   │   ├── exceptions/
│   │   │   │   │   ├── AccountStatusException.java
│   │   │   │   │   ├── DatabaseException.java
│   │   │   │   │   ├── PasswordUpdateException.java
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── RoleNotFoundException.java
│   │   │   │   ├── AccountService.java
│   │   │   │   ├── AuthorizationService.java
│   │   │   │   ├── EmailConfirmationService.java
│   │   │   │   ├── EmailForgotPasswordService.java
│   │   │   │   ├── FileAnalysisService.java
│   │   │   │   ├── FileClamavRuleService.java
│   │   │   │   ├── FileMetadataService.java
│   │   │   │   ├── FileService.java
│   │   │   │   ├── IntegrationsService.java
│   │   │   │   ├── UserFilesAssociationService.java
│   │   ├── config/
│   │   │   ├── CorsConfig.java
│   │   │   ├── InternacionalizacaoConfig.java
│   │   │   ├── SwaggerConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   ├── domain/
│   │   │   ├── entities/
│   │   │   │   ├── Account.java
│   │   │   │   ├── EmailConfirmation.java
│   │   │   │   ├── EmailForgotPassword.java
│   │   │   │   ├── File.java
│   │   │   │   ├── FileAnalysis.java
│   │   │   │   ├── FileClamavRule.java
│   │   │   │   ├── FileMetadata.java
│   │   │   │   ├── Integrations.java
│   │   │   ├── enuns/
│   │   │   │   ├── AccountRole.java
│   │   │   │   ├── MathStatus.java
│   │   │   │   ├── RequestLimit.java
│   │   │   │   ├── YaraMathStatus.java
│   │   │   ├── repositories/
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── EmailConfirmationRepository.java
│   │   │   │   ├── EmailForgotPasswordRepository.java
│   │   │   │   ├── FileAnalysisRepository.java
│   │   │   │   ├── FileClamavRuleRepository.java
│   │   │   │   ├── FileMetadataRepository.java
│   │   │   │   ├── FileRepository.java
│   │   │   │   ├── IntegrationsRepository.java
│   │   │   │   ├── UserFilesAssociationRepository.java
│   │   │   ├── validators/
│   │   │   │   ├── constraintValidators/
│   │   │   │   │   ├── EmailFormatValidator.java
│   │   │   │   │   ├── NameFormatValidator.java
│   │   │   │   │   ├── PasswordFormatValidator.java
│   │   │   │   │   ├── ProfileFormatValidator.java
│   │   │   │   ├── ValidEmail.java
│   │   │   │   ├── ValidName.java
│   │   │   │   ├── ValidPassword.java
│   │   │   │   ├── ValidProfile.java
│   │   │   ├── gateways/
│   │   │   │   ├── rest/
│   │   │   │   │   ├── controllers/
│   │   │   │   │   │   ├── AccountController.java
│   │   │   │   │   │   ├── AuthenticationController.java
│   │   │   │   │   │   ├── FileController.java
│   │   │   │   │   │   ├── ResourceExceptionHandler.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── AccountDataResponseDTO.java
│   │   │   │   │   │   ├── AuthenticationDTO.java
│   │   │   │   │   │   ├── ConfirmEmailDTO.java
│   │   │   │   │   │   ├── DeleteFileDTO.java
│   │   │   │   │   │   ├── FileDataResponseDTO.java
│   │   │   │   │   │   ├── FilesDataResponseDTO.java
│   │   │   │   │   │   ├── ForgotPasswordBodyDTO.java
│   │   │   │   │   │   ├── ForgotPasswordParamsDTO.java
│   │   │   │   │   │   ├── GetFilesDTO.java
│   │   │   │   │   │   ├── RegisterDTO.java
│   │   │   │   │   │   ├── ResendEmailDTO.java
│   │   │   │   │   │   ├── UpdateAccountDTO.java
│   │   │   │   │   │   ├── UpdateFileDTO.java
│   │   │   │   │   │   ├── UpdateIntegrationsDTO.java
│   │   │   │   │   │   ├── UpdatePasswordDTO.java
│   │   │   │   │   │   ├── ValidateTokenDTO.java
│   │   │   │   │   ├── responses/
│   │   │   │   │   │   ├── StandardErrorResponse.java
│   │   │   │   │   │   ├── StandardErrorsResponse.java
│   │   │   │   │   │   ├── StandardGetResponse.java
│   │   │   │   │   │   ├── StandardLoginResponse.java
│   │   │   │   │   │   ├── StandardResponse.java
│   │   │   │   │   ├── ApiErrors.java
│   │   │   │   ├── websocket/
│   │   │   │   │   ├── client/
│   │   │   │   │   │   ├── callbacks/
│   │   │   │   │   │   │   ├── ClamavCallback.java
│   │   │   │   │   │   │   ├── MetadataCallback.java
│   │   │   │   │   │   │   ├── ParserDEXCallback.java
│   │   │   │   │   │   │   ├── ParserELFCallback.java
│   │   │   │   │   │   │   ├── ParserMACHOCallback.java
│   │   │   │   │   │   │   ├── ParserPECallback.java
│   │   │   │   │   │   │   ├── ScanCallback.java
│   │   │   │   │   │   │   ├── ScanYaraCallback.java
│   │   │   │   │   │   ├── EngineBinaryDEXClient.java
│   │   │   │   │   │   ├── EngineBinaryELFClient.java
│   │   │   │   │   │   ├── EngineBinaryMACHOClient.java
│   │   │   │   │   │   ├── EngineBinaryPEClient.java
│   │   │   │   │   │   ├── EngineClamavClient.java
│   │   │   │   │   │   ├── EngineMetadataClient.java
│   │   │   │   │   │   ├── EngineScanClient.java
│   │   │   │   │   │   ├── EngineScanYaraClient.java
│   │   │   │   │   ├── interceptor/
│   │   │   │   │   │   ├── JwtHandshakeInterceptor.java
│   │   │   │   │   ├── server/
│   │   │   │   │   │   ├── ParserBinaryDEXServer.java
│   │   │   │   │   │   ├── ParserBinaryELFServer.java
│   │   │   │   │   │   ├── ParserBinaryMACHOServer.java
│   │   │   │   │   │   ├── ParserBinaryPEServer.java
│   │   │   │   │   │   ├── QuickScanServer.java
│   │   │   │   │   │   ├── SecuredScanServer.java
│   │   │   │   ├── ResourceResponseHandler.java
│   │   │   │   ├── StandardWebSocketResponseMessage.java
│   │   │   ├── infra/
│   │   │   │   ├── security/
│   │   │   │   │   ├── SecurityConfigurations.java
│   │   │   │   │   ├── SecurityFilter.java
│   │   │   │   │   ├── TokenService.java
│   ├── Application.java
├── resources/
│   ├── db/
│   │   ├── changelog/
│   │   │   ├── db.changelog-master.yaml
│   ├── templates/
│   │   ├── forgot-password.html
│   │   ├── verification-email.html
│   ├── application.propertiers
│   ├── banner.txt
│   ├── log-messages.propertiers
│   ├── logback-spring.xml
│   ├── messages.propertiers
````

## Componentes principais

<hr>

# Endpoints e Rotas

## Lista de endpoints disponíveis na API

## Descrição de cada rota

<hr>

# Autenticação e Autorização

## Como os usuários autenticam-se na API

## Políticas de autorização e permissões

<hr>

# Testes e Validação

## Estratégias de testes

## Exemplos de testes e validação

<hr>

# Boas Práticas e Padrões

## Convenções de nomenclatura

## Padrões de codificação e estilo

<hr>

# Referências Externas

## Links para documentos relacionados

## Referências a bibliotecas ou frameworks utilizados

<hr>

# Monitoramento e Logs

## Como os logs são gerados e armazenados

## Descrição dos mecanismos de monitoramento da API

<hr>

# Segurança

## Práticas de segurança recomendadas

## Políticas de segurança

<hr>

# Escalabilidade e Desempenho

## Estratégias para dimensionamento horizontal ou vertical

## Otimização de consultas de banco de dados e cache

<hr>

# Licença e Direitos Autorais