PROJECT TITLE: Slot Machine (inspirado en el Problem I, ICPC World Finals 2025)
PURPOSE OF PROJECT: Simular una tragamonedas de n ruedas y n simbolos de colores,
  con tipos de ruedas y simbolos, y resolver/simular el problema de la maraton.
VERSION or DATE: Ciclo 4/4 - Octubre 2026
HOW TO START THIS PROJECT: Abrir en BlueJ. Para el simulador, crear un objeto SlotMachine
  y llamar makeVisible(). Para el problema: SlotMachineContest.solve(n) / simulate(n).
  Pruebas de aceptacion (manuales): ver GUIA_PRUEBAS_ACEPTACION.md.
AUTHORS: Juan Jose Sanabria Espinel, Tomas Alejandro Melo Rangel
USER INSTRUCTIONS:
  Ruedas (addWheel(tipo,pos)): normal (ventana blanca), lefty (azul, al girar copia a su
    vecina izquierda), rebel (roja, no se deja lock/swap/delete), rainbow (verde, al girar
    adopta el simbolo mas frecuente entre las demas ruedas).
  Simbolos (addSymbol(tipo,pos,color)): normal, ephemeral (se encoge en cada giro hasta un
    punto), shy (alterna visible/invisible cada vez que un giro lo deja seleccionado).
  Pruebas: SlotMachineC4Test, SlotMachineCC4Test (compartidas), y las de ciclos anteriores.
