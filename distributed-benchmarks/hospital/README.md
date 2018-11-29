# Hospital Benchmark

Case study developed and published in SecQL paper 2018.

## Queries

Queries 1-4 (HospitalBenchmark1-4) define the same query, but defer in the privacy tainting properties. During the test in each iteration a person a patient record get published on the respective nodes with the same personId. How often the selection takes placed is defined by personSelectionInterval.

### Base Query

```sql
SELECT DISTINCT person.personId, person.name, knowledge.diagnosis
FROM personDB, UNNEST(patientDB, (p: Patient) => p.symptoms), knowledgeDB
WHERE
	person.personId == patient.personId AND
	patient.symptom == knowledgeData.symptom AND
	knowledgeData.symptom == Symptoms.cough AND
	person.name == "John Doe"
```

### Taints

#### Query1

| Entity | Taints |
| ------ | ------ |
| person-db | red |
| patient-db | green |
| knowledge-db | purple |
| personHost | red |
| patientHost | red, green, purple |
| knowledgeHost | purple |
| clientHost | red, green, purple |

=> Patient and Client host may access all data


#### Query2

| Entity | Taints |
| ------ | ------ |
| person-db | red (gets reclassed to green) |
| patient-db | green |
| knowledge-db | purple |
| personHost | red |
| patientHost | red, green, purple |
| knowledgeHost | purple |
| clientHost | red, green, purple |

=> Patient and Client host may access all data, selection by name can not happen on person-db node


#### Query3

| Entity | Taints |
| ------ | ------ |
| person-db | red (gets reclassed to white) |
| patient-db | green (gets reclassed to white) |
| knowledge-db | purple (gets reclassed to white) |
| personHost | red |
| patientHost | red, green, purple |
| knowledgeHost | purple |
| clientHost | white, red, green, purple |

=> Only client may handle data


#### Query4

| Entity | Taints |
| ------ | ------ |
| person-db | red |
| patient-db | green |
| knowledge-db | purple |
| personHost | red |
| patientHost | red, green, purple |
| knowledgeHost | purple |
| clientHost | white |

=> Query root gets reclassed to white
=> Patient host may access all data, client only result

