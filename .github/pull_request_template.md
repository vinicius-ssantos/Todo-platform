# 🚀 Template de Pull Request

> Siga este modelo para padronizar suas PRs. Títulos devem usar Conventional Commits: `tipo(escopo)?: assunto` — no imperativo, sem ponto final. Exemplos: `feat(api): adiciona endpoint de consulta de CEP`, `fix(log): corrige máscara de dados sensíveis`.

## Resumo
Descreva, em 1–3 frases, o que esta PR faz e por quê.

## Tipo de mudança
- [ ] feat: nova funcionalidade
- [ ] fix: correção de bug
- [ ] refactor: refatoração sem mudança de comportamento
- [ ] perf: melhoria de performance
- [ ] docs: documentação
- [ ] test: testes
- [ ] build: mudanças de build/depêndencias
- [ ] ci: ajustes de CI/CD
- [ ] chore: tarefas de manutenção
- [ ] revert: reversão de mudança

## Issue relacionada
Referencie issues (ex.: Closes #123, Related to #456)

## Como testar
Passos claros para validar a mudança localmente e/ou em ambiente:
1. 
2. 
3. 

Comandos úteis (se aplicável):
```
./mvnw -B -ntp verify
```

## Evidências (logs/prints)
Inclua logs relevantes, prints de telas ou resultados de testes quando fizer sentido.

## Checklist de qualidade
- [ ] Título segue Conventional Commits (ex.: `feat(core): ...`)
- [ ] Testes locais passaram
- [ ] CI está verde
- [ ] Cobertura não diminuiu significativamente (quando aplicável)
- [ ] Documentação atualizada (README, OpenAPI, comentários)
- [ ] Sem breaking changes ocultas (ou descreva abaixo)

## Breaking changes
Descreva se houver (contratos de API, payloads, configs, etc.).

## Riscos e mitigação
Riscos conhecidos e como mitigá-los/monitorá-los.

## Notas de deploy
Flags, migrações, variáveis de ambiente, ou passos especiais.
