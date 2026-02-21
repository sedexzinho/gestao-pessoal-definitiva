# Análise de Problemas - Revenue Controller e Repository

## Problema Identificado: Campo Incorreto no Método `atualizarReceita`

### Localização

Arquivo: `src/main/java/com/example/Controller/RevenueController.java`
Método: `atualizarReceita` (linhas 55-68)

### Código Original (COM ERRO)

```java
@PutMapping("/{id}")
public ResponseEntity<Revenue> atualizarReceita(@PathVariable Long id, @RequestBody RevenueDTO dto) {
  return revenueRepository.findById(id)
      .map(revenue -> {
        revenue.setName(dto.nome());
        revenue.setAmount(dto.valor());
        revenue.setType(dto.tipo());
        if (dto.diaVencimento() != null) {
          revenue.setDueDay(dto.dataRecebimento()); // ❌ ERRADO
        }
        Revenue update = revenueRepository.save(revenue);
        return ResponseEntity.ok(update);
      }).orElse(ResponseEntity.notFound().build());
}
```

### Explicação do Problema

No DTO `RevenueDTO`, existem **dois campos distintos**:

| Campo             | Tipo      | Propósito                          |
| ----------------- | --------- | ---------------------------------- |
| `diaVencimento`   | LocalDate | Data de vencimento da receita      |
| `dataRecebimento` | LocalDate | Data em que a receita foi recebida |

O problema está na linha 63:

- O código verifica `dto.diaVencimento()` (campo de vencimento)
- Mas atribui a **`dto.dataRecebimento()`** ao `dueDay`

Isso causa uma **inconsistência lógica**:

1. O campo `dueDay` no modelo representa o **dia de vencimento** (ou dia esperado para recebimento)
2. Ao usar `dataRecebimento`, está atribuindo a data errada

### Solução Correta

```java
@PutMapping("/{id}")
public ResponseEntity<Revenue> atualizarReceita(@PathVariable Long id, @RequestBody RevenueDTO dto) {
  return revenueRepository.findById(id)
      .map(revenue -> {
        revenue.setName(dto.nome());
        revenue.setAmount(dto.valor());
        revenue.setType(dto.tipo());
        if (dto.diaVencimento() != null) {
          revenue.setDueDay(dto.diaVencimento()); // ✅ CORRETO
        }
        Revenue update = revenueRepository.save(revenue);
        return ResponseEntity.ok(update);
      }).orElse(ResponseEntity.notFound().build());
}
```

---

## Resumo das Correções Aplicadas

| Arquivo                   | Problema                                                                                | Status       |
| ------------------------- | --------------------------------------------------------------------------------------- | ------------ |
| `RevenuesRepository.java` | Query `sumMiscellaneousAmount` usava `status = 'AVULSO'`                                | ✅ Corrigido |
| `RevenueController.java`  | Método `atualizarReceita` usava `dto.dataRecebimento()` em vez de `dto.diaVencimento()` | ⏳ Pendente  |

---

_Documento criado em: 2026-02-20_
