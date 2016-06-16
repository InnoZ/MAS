#R script for reading an sas file (mop) and write the data into a table
#paths need to be adapted for your purposes
library(haven)

#read sas file
d <- read_sas("/home/dhosse/Dokumente/10_data/mop/2002/sas/hh02.sas7bdat")

#The part down to write.data is not used for the actual process
###############################################################
#filter by bundesland and raumtyp (or sth else)
filter <- (d$sgtyp == 40 )&( d$bland == 9)

#filtering of the data
data <- d[filter,]
###############################################################

#write the filtered data into a table (text) file. sep stands for the separating character
#set column and row names to "false" (no header and no column indices will be produced)
write.table(data, "/home/dhosse/Dokumente/10_data/mop/2002/csv/Haushalte.csv", sep=";",col.names=F,row.names=F)
