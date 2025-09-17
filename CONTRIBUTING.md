# Contribuição – Padrões de Commits e Branches

> Projeto: **Todo-platform**

## Objetivos
- Padronizar mensagens de commit e nomes de branch.
- Facilitar revisão, changelog e CI/CD.
- Garantir consistência e clareza.

---

## Fluxo de Branches
- `master` (ou `main`): estável, apenas via PR aprovado + CI verde.
- Prefixos:
  - `feature/` – nova funcionalidade
  - `fix/` – correção de bug
  - `refactor/` – refatoração
  - `perf/` – performance
  - `test/` – testes
  - `docs/` – documentação
  - `chore/` – manutenção
  - `ci/` – pipelines
  - `hotfix/` – correção urgente

**Formato:**
```
<prefix>/<ticket>-<slug-curto>
```

Exemplos:
- `feature/AP11PDP-2629-criar-endpoint-de-listagem`
- `fix/TASK-184-nullpointer-no-activity-consumer`

---

## Padrão de Commits (Conventional Commits)
**Formato:**
```
<tipo>(<escopo>): <resumo>

[corpo - opcional]

[footer - opcional: BREAKING CHANGE:, refs #ticket]
```

**Tipos:** `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `chore`, `ci`, `revert`  
**Escopos sugeridos:** `common`, `api-gateway`, `task-service`, `activity-service`, `infra`, `docs`, `build`

**Regras:**
- Até 72 caracteres, no imperativo.
- Em PT-BR, claro e objetivo.
- Corpo opcional explica o porquê.
- `BREAKING CHANGE:` para mudanças incompatíveis.

Exemplos:
```
feat(task-service): adiciona criação de task e evento Kafka
fix(activity-service): corrige trusted.packages inseguro
refactor(common): usa enum TaskStatus na entidade
```

---

## Pull Requests
- Título segue padrão de commit.
- `Squash & Merge` recomendado.
- Referenciar tickets: `refs AP11PDP-2629`, `closes TASK-184`.

---

## Checklists

### Antes de commitar
- [ ] Código compila (`mvn verify`).
- [ ] Testes locais passam.
- [ ] Mensagem segue padrão.
- [ ] Sem `System.out` ou lixo de debug.
- [ ] Docs/testes atualizados se necessário.

### Antes de abrir PR
- [ ] Branch atualizada com `master` (rebase).
- [ ] CI verde.
- [ ] Descrição clara (antes/depois, risco, rollback).
- [ ] Relacionado a ticket (se aplicável).

---

## Versionamento
- **SemVer**: `vMAJOR.MINOR.PATCH`
  - `feat` → MINOR
  - `fix` → PATCH
  - `BREAKING CHANGE` → MAJOR
- Tags anotadas:
```
git tag -a v1.3.0 -m "Release v1.3.0"
git push origin v1.3.0
```

---

## Automação
- Changelog automático via Conventional Commits.
- Commitlint/Gitlint para validar mensagens.
- Hook pré-commit para validar padrão de commit:

```bash
#!/usr/bin/env bash
msg_file="$1"
pattern='^(feat|fix|refactor|perf|test|docs|chore|ci|revert)(\([a-z0-9-]+\))?: .{1,72}$'
if ! grep -Pq "$pattern" "$msg_file"; then
  echo "✖ Commit fora do padrão Conventional Commits."
  exit 1
fi
```

---

## FAQ
- Emoji? Evite, priorizamos compatibilidade.
- Inglês? Aceito, mas preferir PT-BR consistente.
- Vários tipos num commit? Evite, faça commits separados.
