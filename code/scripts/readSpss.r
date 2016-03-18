#R script for reading an spss file and write the data into a table
#paths need to be adapted for your purposes
library(foreign)

#read spss file
#use.value.labels = if labels that are assigned to numeric values should be displayed instead of the values (e.g. 1 for "has driving license" in column "license")
#to.data.frame = packs the data into a data frame. makes some of the stuff in this script easier
d <- read.spss("/home/dhosse/Dokumente/10_data/MiD2008/spss/MiD2008_PUF_Wege.sav", use.value.labels=FALSE, to.data.frame=TRUE)

#filter by bundesland and raumtyp (or sth else)
filter <- (d$sgtyp == 40 )&( d$bland == 9)

#filtering of the data
data <- d[filter,]

#write the filtered data into a table (text) file. sep stands for the separating character
write.table(data, "/home/dhosse/Dokumente/10_data/MiD2008/csv/MiD2008_PUF_Wege2.csv", sep=";")
