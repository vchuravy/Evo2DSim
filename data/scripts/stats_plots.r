#!/usr/bin/env Rscript
# R-Script to plot the mean data of several csv scripts
# first argument is tou output filename
require(ggplot2)
require(reshape)

arguments <- commandArgs(trailingOnly=TRUE)
fbase  <- arguments[1]
arguments <- arguments[-1]

extractName <- function(path) {
    clean_name <- sub("\\d{4}_(\\d{2}_){5}", "", path , perl=TRUE)
    split_name <- strsplit(clean_name, "/")[[1]]
    name <- (tail(split_name, 2))[1]
    return(name)
}

# Load files as csv
files <- Map(function(x) paste(x, "Stats.csv", sep=""), arguments)

# Merge data into one frame
loadData <- function(fn){
     tmp <- read.csv(fn)
     extractName(fn)
     tmp$Filename <- extractName(fn)
     tmp
}

dataList <- lapply(files, loadData)

data <- do.call(rbind, dataList)
# Merge data together to one data group
# mean <- do.call("cbind", Map(function(x) x["Mean"], files))

#Create pdf
ftype <- "svg"
w <- 10
h <- 5

plot_var <- function(var_name) {
  var_data <- data[c("Filename", "Generation", var_name)]
  df <- melt(var_data, id=c("Filename", "Generation"), variable_name = "Filename")
  image <- ggplot(df, aes(Generation, value)) + geom_line(aes(group = Filename, colour = Filename)) + ggtitle(var_name)
  filename <- paste0(fbase, "_", var_name, ".", ftype)
  ggsave(file=filename, plot=image, width=w, height=h)
}

n <- names(data)
n <- n[! n %in% c("Filename", "Generation")]

lapply(n, plot_var)
