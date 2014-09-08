* Anzahl der Personen im Kreistyp ländlicher Raum (sgtyp=40) und Bundesland Bayern bestimmen 

DATASET ACTIVATE MiD2008Personen.

USE ALL. 
COMPUTE filter_$=(sgtyp = 40 & bland=9). 
VARIABLE LABELS filter_$ 'sgtyp = 40 & bland=9 (FILTER)'. 
VALUE LABELS filter_$ 0 'Not Selected' 1 'Selected'. 
FORMATS filter_$ (f1.0). 
FILTER BY filter_$. 
EXECUTE.

CTABLES 
  /VLABELS VARIABLES=hp_sex hp_altg2 DISPLAY=LABEL 
  /TABLE hp_sex > hp_altg2 [COUNT F40.0] 
  /CATEGORIES VARIABLES=hp_sex hp_altg2 ORDER=A KEY=VALUE EMPTY=INCLUDE MISSING=EXCLUDE.


