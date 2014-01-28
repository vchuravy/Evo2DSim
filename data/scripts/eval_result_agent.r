#!/usr/bin/env Rscript
# R-script to extract the interesting activity data from a simulation run
arguments <- commandArgs(trailingOnly=TRUE)
fbase  <- arguments[1]

outputDir <- paste0(fbase, "output")
outputFile <- paste0(fbase, "SignalStrategy.csv")

generations <- list.dirs(outputDir, recursive = FALSE, full.names = FALSE)


calcGA <- function(generation) {
    genDir <- paste0(outputDir, "/", generation)
    groups <- list.dirs(genDir, recursive = FALSE, full.names = FALSE)
    data <- lapply(groups, calcGroupActivity, generation = generation)
    data <- do.call(rbind, data)
    return(data)
}

calcGroupActivity <- function(generation, group) {
    groupDir <- paste0(outputDir, "/", generation, "/", group)
    evals <- list.dirs(groupDir, recursive = FALSE)
    data <- lapply(evals, calcActivity)
    mean <- Reduce("+", data) / length(data)
    result <- c(generation, group, mean)
    return(result)
}


calcActivity <- function(evalDir) {
    agentFiles <- list.files(evalDir, pattern = "\\d+_agent.csv", full.names = TRUE)
    data <- lapply(agentFiles, extractValues)
    data <- do.call(rbind, data) # Join Agents

    nearPoison <- data[data$currentReward < 0, ]
    timeNearPoison <- nrow(nearPoison)
    lightNearPoison <- nrow(nearPoison[nearPoison$light == 'true', ])

    nearFood <- data[data$currentReward > 0, ]
    timeNearFood <- nrow(nearFood)
    lightNearFood <- nrow(nearFood[nearFood$light == 'true', ])

    agentCount <- length(agentFiles)

    f <- lightNearFood / timeNearFood
    p <- lightNearPoison / timeNearPoison

    if(is.nan(f)) {
        f <- 0
    }

    if(is.nan(p)) {
        p <- 0
    }

    signalStrategy <- (f - p) / agentCount
    return(signalStrategy)
}

# Get AgentID, CurrentReward, Light where currentReward != 0
extractValues <- function(file) {
    tmp <- read.csv(file, stringsAsFactors = FALSE, sep=",", strip.white=TRUE)
    tmp <- tmp[c("currentReward", "light")]
    tmp$AgentID <- extractID(file)
    tmp <- tmp[tmp$currentReward != 0, ]
    return(tmp)
}

extractID <- function(path) {
    split_name <- strsplit(path, "/")[[1]]
    filename <- (tail(split_name, 1))[1]
    id <- head(strsplit(filename, "_"))[[1]][1]
    return(id)
}

result <- lapply(generations, calcGA)
result <- do.call(rbind, result)
colnames(result) <- c("Generation", "Group", "SignalStrategy")
result <- as.data.frame(result)
write.table(result, file=outputFile, sep=", ", row.names=FALSE, quote = FALSE)