# Killer Kiss: web + API + SQLite en un sol procés (Spring Boot)
LOG := app.log
PID := app.pid
URL := http://localhost:8080/public.html

.PHONY: up down restart logs status

up: ## Arrenca l'app en segon pla i espera que estigui llesta
	@if [ -f $(PID) ] && kill -0 $$(cat $(PID)) 2>/dev/null; then echo "Ja està en marxa: $(URL)"; exit 0; fi; \
	setsid nohup ./mvnw -q spring-boot:run > $(LOG) 2>&1 & echo $$! > $(PID); \
	echo "Arrencant..."; \
	for i in $$(seq 1 120); do \
		if grep -q "Started Main" $(LOG); then echo "Llest: $(URL)"; exit 0; fi; \
		if grep -qE "APPLICATION FAILED|BUILD FAILURE" $(LOG) || ! kill -0 $$(cat $(PID)) 2>/dev/null; then \
			echo "Error en arrencar. Mira $(LOG)"; tail -20 $(LOG); rm -f $(PID); exit 1; fi; \
		sleep 1; \
	done; echo "Encara no ha arrencat després de 120 s. Mira $(LOG)"; exit 1

down: ## Atura l'app
	@if [ -f $(PID) ] && kill -TERM -$$(cat $(PID)) 2>/dev/null; then \
		while kill -0 -$$(cat $(PID)) 2>/dev/null; do sleep 0.5; done; echo "Aturada"; \
	else echo "No estava en marxa"; fi; rm -f $(PID)

restart: down up ## Reinicia (cal després de canviar mail.config)

logs: ## Mostra el registre en directe (Ctrl+C per sortir)
	@tail -f $(LOG)

status: ## Diu si l'app està en marxa
	@if [ -f $(PID) ] && kill -0 $$(cat $(PID)) 2>/dev/null; then echo "En marxa: $(URL)"; else echo "Aturada"; fi
