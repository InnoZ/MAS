# Script to generate a pie chart of the modal split
# This needs two inputs: A MATSim file (sth) and an output path

#!/usr/bin/env Rscript
# Read in the runtime arguments
args = commandArgs(trailingOnly=TRUE)

if(length(args)<2){

stop("You need to specify two arguments for that call (input MATSim file and output image file). Aborting.", call.=FALSE)

} else {

options(scipen=5)

# Read args[1] (input file)
data <- read.table(args[1],header=TRUE,sep="\t")

labels <- list()
values <- list()

i = 10
n = 0

time <- data[2]

while(i <= length(data)){

name <- sub("en.route_","",names(data[i]))
value <- data[i]

if(name != "transit_walk"){

labels<-c(labels, name)
values<-c(values, value)
n<-n+1

}

i<-i+4

}

# Plot the chart to the location defined in the runtime args
png(filename=args[2],width=800,height=600)

# Create the chart
labels <- unlist(labels)
values <- unlist(values)
valueMatrix <- matrix(values,ncol=n)

matplot(time/3600,valueMatrix,pch=1, lty=1, type="l", main="Leg histogram", xlab="time of day [h]", ylab="number of travelers en route [ ]",bty="L")
legend("right",labels,xpd=TRUE,horiz=FALSE,col=c(1:n),lty=c(1))

dev.off()

if(length(args)>2){

allValues <- data[6]
allLabel <- sub("en.route_", "", names(data[6]))

png(filename=args[3], width=800,height=600)

matplot(time/3600,allValues,pch=1, lty=1, type="l", main="Leg histogram", xlab="time of day [h]", ylab="number of travelers en route [ ]",bty="L")
legend("right",allLabel,xpd=TRUE,horiz=FALSE,col=c(1),lty=c(1))

dev.off()

}

}
