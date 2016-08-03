# Script to generate a pie chart of the modal split
# This needs two inputs: A MATSim file (sth) and an output path

#!/usr/bin/env Rscript
# Read in the runtime arguments
args = commandArgs(trailingOnly=TRUE)

if(length(args)<2){

stop("You need to specify two arguments for that call (input MATSim file and output image file). Aborting.", call.=FALSE)

} else {

# Read args[1] (input file)
data <- read.table(args[1],header=TRUE,sep="\t")

labels <- list()
values <- list()

i = 7
while(i < length(data)){

name <- sub("departures_","\\2",names(data[i]))
sum <- sum(data[i])

if(name != "transit_walk"){

labels<-c(labels, name)
values<-c(values, sum)

}

i<-i+4

}

# Plot the chart to the location defined in the runtime args
png(filename=args[2])

# Create the chart
labels <- unlist(labels)
values <- unlist(values)

pct <- round(values/sum(values)*100)
labels <- paste(labels, pct)
labels <- paste(labels, "%", sep="")

chart <- pie(values, labels, main="Modal Split")

dev.off()

}
