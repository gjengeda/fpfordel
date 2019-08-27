ALTER TABLE "PROSESS_TASK_TYPE"
  ADD ("CRON_EXPRESSION" VARCHAR2(200 CHAR) NULL );


COMMENT 
ON 
COLUMN 
"PROSESS_TASK_TYPE"
.
"CRON_EXPRESSION" 
IS 
'Cron-expression for n�r oppgaven skal kj�res p� nytt';


alter table prosess_task add (SISTE_KJOERING_PLUKK_TS timestamp(6));
alter table prosess_task add (SISTE_KJOERING_SLUTT_TS timestamp(6));

COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_TS" IS 'siste gang tasken ble fors�kt kj�rt (f�r kj�ring)';
COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_SLUTT_TS" IS 'tidsstempel siste gang tasken ble kj�rt (etter kj�ring)';
COMMENT ON COLUMN "PROSESS_TASK"."SISTE_KJOERING_PLUKK_TS" IS 'siste gang tasken ble fors�kt plukket (fra db til in-memory, f�r kj�ring)';

alter table prosess_task drop constraint chk_prosess_task_status;
alter table prosess_task add constraint "CHK_PROSESS_TASK_STATUS" check (status in ('KLAR', 'FEILET', 'VENTER_SVAR', 'SUSPENDERT', 'VETO', 'FERDIG')) enable;

-- Ny partisjon: opprettes kun i 12c, XE st�tter ikke
BEGIN

  IF (DBMS_DB_VERSION.VERSION > 11) THEN
    execute immediate 'ALTER TABLE PROSESS_TASK MODIFY PARTITION STATUS_KLAR ADD VALUES (''VETO'')';
  END IF;

END;

-----------------------------------------------------------
-- OPPDATERT PER FP-FELLES 1.1.0_20190329073313_e0399cb ---
-----------------------------------------------------------
