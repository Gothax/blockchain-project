# blockchain-project

Bitcoin Full node에서 진행되는 트랜잭션 검증을 흉내내는 가상의 스택 기반 실행 엔진 설계, 구현 <br>
P2PKH, P2SH, Multisignature 방식의 스크립트를 실행시켜 결과를 도출

![image](https://github.com/user-attachments/assets/0175d264-b137-4270-8115-87580c718d4e)


### Structure

![image](https://github.com/user-attachments/assets/21fd60d1-63ee-4b90-8c2d-059d6d791f95)



### 주요 기능

- Full Node <br>
트랜잭션 검증 <br>
UTXO set 수정 <br>
Query Process 질의에 응답


- Execute Engine <br>
하나의 트랜잭션 처리 <br>
Amount 검증 (스택 공간을 가지는 Operator 인스턴스로) <br>
Script 검증 <br>
```java
      public static boolean validate(Transaction transaction) {
          return validateAmount(transaction) && validateScript(transaction);
      }
      
      private static boolean validateScript(Transaction transaction) {
        // 검증마다 새로운 stack 할당하기 위해 Operator 객체 생성
        String lockingScript = transaction.getInput().getLockingScript();
        String unlockingScript = transaction.getUnlockingScript();
        String prevTxHash = transaction.getInput().getPtxHash();
        Operator operator = new Operator(lockingScript, unlockingScript, prevTxHash);
      
        return operator.validate();
      }
```

- Operator <br>
locking script + unlocking script 실행 <br>
validate -> start -> execute 흐름 <br>

  validate: P2SH 분기 <br>
  - locking script에 OP_CHECKFINALRESULT 없으면 P2SH 방식인 것을 이용 <br>
  - unlocking script에서 scriptX 추출 <br>

  start: IF ELSE ENDIF 분기 <br>
  - skip 변수로 해결

  execute: OP_CODE 실행 <br>
  - OP 코드가 아니라면 stack에 push <br>
  - DUP, HASH, EQUAL, EQAULVERIFY
  - CHECKSIG, CHECKSIGVERIFY
  - CHECKMULTISIG, CHECKMULTISIGVERIFY

  
- Repository <br>
UTXO set, Transaction set 관리 <br>
Singleton 패턴으로 구현 <br>
  - query process 개발 과정에서 멀티 스레드 환경 안정성을 위해 적용

  
- Query Process <br>
  ![image](https://github.com/user-attachments/assets/156804d8-4fe5-4a7b-8b9f-ab07251b80f7) <br>
FullNode와 멀티스레드 환경으로 병렬 실행 <br>
FullNode static method로 질의 요청 <br>
(Full Node가 transaction 처리중) lock 획득 못할시  busy waiting <br>
응답받은 SnapShot 출력 <br>
  ```text
  =========snapshot transactions=========
  transaction: ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, validity check:passed
  transaction: Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, validity check:passed
  ================================
  =========snapshot utxo=========
  tx3,  0,  100,  OP_DUP OP_HASH ZYcPB3VLkkW6XIa6ds1paI5nuqn7iLcYiZtX3BjZHqk= OP_EQUALVERIFY
  tx4,  0,  100,  OP_DUP OP_HASH W7Aei6dgXgLKeVtZ0JlDrvq4iImHr2P9TODNDGiij/s= OP_EQUALVERIFY
  tx4,  1,  200,  OP_DUP OP_HASH tofRoa1lF3313xy745y9hpgz/WYd1W/iKR/dR+Oz7pc= OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
  ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
  ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
  Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 0, 30, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
  Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 1, 60, 2 <pubKey1> <pubKey2> <pubKey3> 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT
  ================================
  ```

- Data (transactions.txt, UTXOes.txt) <br>
transactions.txt에는 <--> 대신 실제 값을 넣음 <br>
예제
  - P2PKH
    ```text
    tx:
    input: tx1, 0, 100, OP_DUP OP_HASH <public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT, <signature> <public key>
    output, 0: 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output, 1: 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    ```
    Alice가 물건을 사는 상황 (buyer : Alice) <br>
    이전 transaction 내용 hash값이 tx1이라고 가정
  - MULTISIG
    ```text
    tx:
    input: tx2, 0, 100, 2 <pubKey1> <pubKey2> <pubKey3> 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT, <signature1> <signature2>
    output, 0: 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output, 1: 40, 2 <pubKey1> <pubKey2> <pubKey3> 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT
    ```
    3명으로 구성된 조직에서 Alice와 Bob의 signature로 물건을 사는 상황 <br>
    3개의 key pair 생성 후 valid한 signature 2개 삽입
  - P2SH

    ```text
    scriptX
    OP_IF
        OP_DUP OP_HASH <public key hash> OP_EQUALVERIFY OP_CHECKSIG
    OP_ELSE
        2 <Alice pubKey> <Bob pubKey> <pubKey3> 3 OP_CHECKMULTISIG
    OP_ENDIF
        OP_CHECKFINALRESULT
    ```
    ```text
    Alice가 spend하는 상황 (IF로 분기) <br>
    unlockingScript : <Alice signature> <AlicePubKey> 1 <script X> <br>
    
    tx:
    input: tx3, 0, 100, OP_DUP OP_HASH <script X hash> OP_EQUALVERIFY, <Alice signature> <AlicePubKey> 1 <script X>
    output, 0: 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output, 1: 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    ```

    ```text
    Alice와 Bob이 함께 spend하는 상황 (ELSE로 분기) 
    unlockingScript : <Alice signature> <Bob signature> 0 <scriptX>
    
    tx:
    input: tx3, 0, 100, OP_DUP OP_HASH <script X hash> OP_EQUALVERIFY, <Alice signature> <Bob signature> 0 <script X>
    output, 0: 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output, 1: 10, OP_DUP OP_HASH <Alice public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output, 2: 30, OP_DUP OP_HASH <Bob public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    ```


### Tests
![image](https://github.com/user-attachments/assets/33317027-7c35-4271-85ae-0320417f6e31)

핵심 로직인 Operator Class 위주로 test <br>
다음으로 입출력 오류를 최소화 하기 위해 Repository test


### 전체 실행 결과
```text
transaction: ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=
    input=tx1, 0, 100, OP_DUP OP_HASH +KxaNOBK9unLhNUWh76R+C+mUB/NZSOoS24q7hdOtsU= OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT,  MEUCIAQGSjvvcYBrlwmJy8FMwbr9bVUo823LHXaBpC+sbHgpAiEAibuAYu7RQ/6qIEjdhgD5zo8+a2qd1PO+W+aQJGnypew= MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc57NsPWZoeM5GBUto5lZ3fypa8WZTgDYX2AcUd+5eoMk/pCxu7Ocljm1h5ixZCKO+VbJHLmgC5ut/G3o2b88Dw==
    output: 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output: 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    validity check: passed

=========snapshot transactions=========
transaction: ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, validity check:passed
================================
=========snapshot utxo=========

tx2,  0,  100,  2 MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEyLswFgpCwtdkhT/jRw+/YmXov9jZ8BJNIerCjUwidlnKbw4X7LRbwJlLjm3lYYmQzdNXxjljZ5m932MDP6UzAQ== MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEluoP2Xj6RnmcXkWBN3SAlJ5ce7Wtd5RVLV5pDK8bpUrgcbLaF6tHb0UuP7l8YWZMrJgIhjhGUHK72IdFINPicA== MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEHKiPIdVVWmU0xTR1ropdJdYS5uNKPSV/RiDRxSO++aPmL/7RzQX9ihE+ZdiX/dkNVxzspafWQkstnAzHvSCbLQ== 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT
tx3,  0,  100,  OP_DUP OP_HASH ZYcPB3VLkkW6XIa6ds1paI5nuqn7iLcYiZtX3BjZHqk= OP_EQUALVERIFY
tx4,  0,  100,  OP_DUP OP_HASH W7Aei6dgXgLKeVtZ0JlDrvq4iImHr2P9TODNDGiij/s= OP_EQUALVERIFY
tx4,  1,  200,  OP_DUP OP_HASH tofRoa1lF3313xy745y9hpgz/WYd1W/iKR/dR+Oz7pc= OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
================================


transaction: Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=
    input=tx2, 0, 100, 2 MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEyLswFgpCwtdkhT/jRw+/YmXov9jZ8BJNIerCjUwidlnKbw4X7LRbwJlLjm3lYYmQzdNXxjljZ5m932MDP6UzAQ== MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEluoP2Xj6RnmcXkWBN3SAlJ5ce7Wtd5RVLV5pDK8bpUrgcbLaF6tHb0UuP7l8YWZMrJgIhjhGUHK72IdFINPicA== MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEHKiPIdVVWmU0xTR1ropdJdYS5uNKPSV/RiDRxSO++aPmL/7RzQX9ihE+ZdiX/dkNVxzspafWQkstnAzHvSCbLQ== 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT,  MEUCIQDcCrtm9qCexNG4DFa0nVU9AvQ3c/8/WoFbAD2bzg8hbgIgTiMZFGBDGiq4itoMQmKHODBqbm+7jyIa1ooG+rs644Y= MEUCID9BzF9tcSxjtrQpilIAaqNBPq/agyUSsjqJikwbTBRNAiEAuAexgjHIs5Bet0HR/W02apqikzfs3bqb54uTW7wJW90=
    output: 0, 30, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output: 1, 60, 2 <pubKey1> <pubKey2> <pubKey3> 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT
    validity check: passed

=========snapshot transactions=========
transaction: ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, validity check:passed
transaction: Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, validity check:passed
================================
=========snapshot utxo=========

tx3,  0,  100,  OP_DUP OP_HASH ZYcPB3VLkkW6XIa6ds1paI5nuqn7iLcYiZtX3BjZHqk= OP_EQUALVERIFY
tx4,  0,  100,  OP_DUP OP_HASH W7Aei6dgXgLKeVtZ0JlDrvq4iImHr2P9TODNDGiij/s= OP_EQUALVERIFY
tx4,  1,  200,  OP_DUP OP_HASH tofRoa1lF3313xy745y9hpgz/WYd1W/iKR/dR+Oz7pc= OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 0, 30, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 1, 60, 2 <pubKey1> <pubKey2> <pubKey3> 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT
================================


transaction: EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=
    input=tx3, 0, 100, OP_DUP OP_HASH ZYcPB3VLkkW6XIa6ds1paI5nuqn7iLcYiZtX3BjZHqk= OP_EQUALVERIFY,  MEQCIA2r90A8SQ63lcKsRLYSQKWJS29F2yLyl03r0tuv337yAiAv8XKyxzy5kIsYQMouTLv+FLCT7lX4rVyi7dy7EAFjVA== MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEwk5N5RNEsq8nUv+av76f+jlyOm7Nyxe+megCBBzQYNjfy9Ssj679g2UCwhDECX8CqrsiLmvXUvr4HWuixq1rjA== 1 OP_IF OP_DUP OP_HASH P3ODGKo2ApXqlVlQZIUqWnMEX9hSl/DoaQxwO5MAHag= OP_EQUALVERIFY OP_CHECKSIG OP_ELSE 2 MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEwk5N5RNEsq8nUv+av76f+jlyOm7Nyxe+megCBBzQYNjfy9Ssj679g2UCwhDECX8CqrsiLmvXUvr4HWuixq1rjA== MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEimUjGCL8WQIvQEzhGYekm6ABwigs36koOBOld09Y4TW9IJVRbP2ARY0aSipVcM+0qX6knayS9GL6rIaQEVTcMw== MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEGPcJIUlTJ6WoyNg7szOqLQTW8Y77xE+MlIk4J7dLN2XusBVzsNysgewcu1HE8ByeCI8mkqeScEsHumcKSN/6zQ== 3 OP_CHECKMULTISIG OP_ENDIF OP_CHECKFINALRESULT
    output: 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output: 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    validity check: passed

=========snapshot transactions=========
transaction: ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, validity check:passed
transaction: Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, validity check:passed
transaction: EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=, validity check:passed
================================
=========snapshot utxo=========

tx4,  0,  100,  OP_DUP OP_HASH W7Aei6dgXgLKeVtZ0JlDrvq4iImHr2P9TODNDGiij/s= OP_EQUALVERIFY
tx4,  1,  200,  OP_DUP OP_HASH tofRoa1lF3313xy745y9hpgz/WYd1W/iKR/dR+Oz7pc= OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 0, 30, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 1, 60, 2 <pubKey1> <pubKey2> <pubKey3> 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT
EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=, 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
================================


transaction: 9WHlJlAXegkbjgCnMWo8OlePesZ63Jsahi1mVvsYhyg=
    input=tx4, 0, 100, OP_DUP OP_HASH W7Aei6dgXgLKeVtZ0JlDrvq4iImHr2P9TODNDGiij/s= OP_EQUALVERIFY,  MEUCIDVRNZGlRYo8OyZHEPkYX3sp/KUE9SHfplvoeJBTocI6AiEA2G627RgQIBmd6pUxowP5bj6HmsviNfE4w2UcooszNbg= MEYCIQDqiqzaeTQefYKInGklZnJttFV5YG9IOlj3YlT8GKJRAgIhALQc1Fc+A+fAVQ1gTKFySaYFwVc2s8kkEsgkwUyfjT3r 0 OP_IF OP_DUP OP_HASH XasEIUdrG0gmg/F9TQnp0ttLpRz/NT0JmldRYTzJuq4= OP_EQUALVERIFY OP_CHECKSIG OP_ELSE 2 MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEbqYWYZLVbbllJCgQYFt/vgxdBVt5Ik/Q1dn6zMa6qHW03LCpMcwD+kN02OvfxSYWppGVKLm6Aso/nZ3cKNvB9g== MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXrczK8cmrbHuB9xJO+nJwHt2/rB66Gj3ScabYDCsjMLzlrgFbLgi5UuHcsvuD97r7n14YLihSlVZyQ50LcxN4g== MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEdVNF/HMzO/+JI5NambXLQpG1oCyE4W+RoV+RadIfVNSrkdyClWG2lvE/GrNXOUquqH603Y8Fnv9La+ynCf+AKA== 3 OP_CHECKMULTISIG OP_ENDIF OP_CHECKFINALRESULT
    output: 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output: 1, 10, OP_DUP OP_HASH <Alice public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output: 2, 30, OP_DUP OP_HASH <Bob public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    validity check: passed

=========snapshot transactions=========
transaction: ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, validity check:passed
transaction: Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, validity check:passed
transaction: EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=, validity check:passed
transaction: 9WHlJlAXegkbjgCnMWo8OlePesZ63Jsahi1mVvsYhyg=, validity check:passed
================================
=========snapshot utxo=========

tx4,  1,  200,  OP_DUP OP_HASH tofRoa1lF3313xy745y9hpgz/WYd1W/iKR/dR+Oz7pc= OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 0, 30, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 1, 60, 2 <pubKey1> <pubKey2> <pubKey3> 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT
EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=, 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
9WHlJlAXegkbjgCnMWo8OlePesZ63Jsahi1mVvsYhyg=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
9WHlJlAXegkbjgCnMWo8OlePesZ63Jsahi1mVvsYhyg=, 1, 10, OP_DUP OP_HASH <Alice public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
9WHlJlAXegkbjgCnMWo8OlePesZ63Jsahi1mVvsYhyg=, 2, 30, OP_DUP OP_HASH <Bob public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
================================


transaction: 76WjXUzEoWowSvUTd7hEAXRBNvY8GFQn6k1OOJGCFOA=
    input=tx4, 1, 200, OP_DUP OP_HASH tofRoa1lF3313xy745y9hpgz/WYd1W/iKR/dR+Oz7pc= OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT,  MEUCIAQGSjvvcYBrlwmJy8FMwbr9bVUo823LHXaBpC+sbHgpAiEAibuAYu7RQ/6qIEjdhgD5zo8+a2qd1PO+W+aQJGnypew= MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc57NsPWZoeM5GBUto5lZ3fypa8WZTgDYX2AcUd+5eoMk/pCxu7Ocljm1h5ixZCKO+VbJHLmgC5ut/G3o2b88Dw==
    output: 0, 200, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    output: 1, 0, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
    validity check: failed
        failed at: OP_EQUALVERIFY 검증 실패

=========snapshot transactions=========
transaction: ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, validity check:passed
transaction: Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, validity check:passed
transaction: EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=, validity check:passed
transaction: 9WHlJlAXegkbjgCnMWo8OlePesZ63Jsahi1mVvsYhyg=, validity check:passed
transaction: 76WjXUzEoWowSvUTd7hEAXRBNvY8GFQn6k1OOJGCFOA=, validity check:failed
================================
=========snapshot utxo=========

tx4,  1,  200,  OP_DUP OP_HASH tofRoa1lF3313xy745y9hpgz/WYd1W/iKR/dR+Oz7pc= OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
ZNzP2roAefHCUKAI+wSSCv3y8/J/uHQNbzhCHIO9rbY=, 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 0, 30, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
Rm2gsKR8JHKGfv/HQVbvak/uKkp6/hobHEH42WTIO5E=, 1, 60, 2 <pubKey1> <pubKey2> <pubKey3> 3 OP_CHECKMULTISIG OP_CHECKFINALRESULT
EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
EIJ3vWixQdmma0TnheGJKUFiXPvCSLDDni4FI/os97s=, 1, 40, OP_DUP OP_HASH <buyer's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
9WHlJlAXegkbjgCnMWo8OlePesZ63Jsahi1mVvsYhyg=, 0, 50, OP_DUP OP_HASH <seller's public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
9WHlJlAXegkbjgCnMWo8OlePesZ63Jsahi1mVvsYhyg=, 1, 10, OP_DUP OP_HASH <Alice public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
9WHlJlAXegkbjgCnMWo8OlePesZ63Jsahi1mVvsYhyg=, 2, 30, OP_DUP OP_HASH <Bob public key hash> OP_EQUALVERIFY OP_CHECKSIG OP_CHECKFINALRESULT
================================
```


### 폴더 구조
```
src/
├── main/
│   ├── java/
│   │   ├── com.gothaxcity/
│   │   │   ├── domain/         // Utxo 및 Transaction 클래스 정의
│   │   │   ├── repository/     // Utxo 및 Transaction 저장소 관리
│   │   │   ├── service/        // Operator, FullNode, ExecuteEngine 클래스
│   │   │   ├── util/           // SHA256 및 ECDSA 암호화 기능
│   ├── resources/
│   │   ├── UTXOes.txt          // 현재 UTXO 상태 저장 파일
│   │   ├── transactions.txt    // 트랜잭션 집합 파일
```
