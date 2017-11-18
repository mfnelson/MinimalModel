# install.packages("ncdf4")
rm(list = ls())

require(ncdf4)
require(animation)

p = "C:/github/MinimalModel/output"
if(dir.exists(p)) outputDir = p
setwd(outputDir)


dataDir = paste0(outputDir, "/fourByFour")
simName = "fourByFour"
nRows = 4; nCols = 4

readNetCdfToArrays = function(dataDir, simName, nRows, nCols){
  
  filenames = array(dim = c(nRows, nCols))
  for(row in 1:nRows) for(col in 1:nCols){
    filenames[row, col] = paste0(
      dataDir, "/", simName, "_row_", row - 1, "_col_", col - 1, ".nc")
  }
  
  filenames
  datNC = list()
  for(row in 1:nRows){
    datCol = list()
    for(col in 1:nCols){
      filename = filenames[row, col]
      ncf = nc_open(filename)
      datCol[[col]] = ncvar_get(ncf, "beetles")
      nc_close(ncf)
    }
    datNC[[row]] = datCol
  }
  
  (dataDims = dim(datNC[[1]][[1]]))
  
  datAll = array(dim = c(dataDims[1], nRows * dataDims[2], nCols * dataDims[3]))    
  year = 1; row = 0; col = 0
  
  dim(datAll)
  (nRowsCell = dataDims[2])
  (nColsCell = dataDims[3])
  
  
  # row = 1
  # col = 1
  for(year in 1:(dataDims[1] - 0)){
    for(col in 1:nCols) for(row in 1:nRows){


      # row = 3
      # col = 3
      # year = 172
      # row = 3;
      datNC[[row]][[col]][year , , ]

      rows = (((row - 1) * nRowsCell):(row * nRowsCell - 1) + 1)
      cols = (((col - 1) * nColsCell):(col * nColsCell - 1) + 1)

      # print(paste0("max in Quadrant row: ", row, " col: ", col," = ",
      #              max(datNC[[row]][[col]][year , , ])))

      rows
      cols
      datAll[year, rows, cols] = datNC[[row]][[col]][year , , ]
      # print(dim(datNC[[row]][[col]][year, , ]))
    }
    maxVals = max(datAll[year , , ])
    datAll[year, , ] = datAll[year, , ] / max(1, maxVals)
    # print(paste0("year = ", year, "; max = ", maxVals))
  }
  return(datAll)
}



nRows = 4; nCols = 4
simName = "fourByFour"
dataDir = paste0(outputDir, "/fourByFour")
datAll = readNetCdfToArrays(dataDir, simName, nRows, nCols)
image(datAll[1, , ])
abline(h = (1 : (nRows - 1)) * (1 / nRows))
abline(v = (1 : (nCols - 1)) * (1 / nCols))

nRows = 1; nCols = 2
simName = "oneByTwo"
dataDir = paste0(outputDir, "/oneByTwo")


nRows = 2; nCols = 2
simName = "twoByTwo"
dataDir = paste0(outputDir, "/twoByTwo")

datAll = readNetCdfToArrays(dataDir, simName, nRows, nCols)
dim(datAll)
(datAll[1, , ])
abline(h = (1 : (nRows - 1)) * (1 / nRows))
abline(v = (1 : (nCols - 1)) * (1 / nCols))

datAll[1, , ]
dim(datAll[1, , ])


sizeFactor = 10
ani.options(interval = 0.1, ani.width = sizeFactor * (dim(datAll)[3]), ani.height = sizeFactor * (dim(datAll)[2]), ffmpeg = "C:/Program Files/ffmpeg/bin/ffmpeg.exe")
saveVideo(expr = {
  for(year in 1:(dim(datAll)[1] - 1)){
    par(mar = c(0, 0, 0, 0)); image(t(datAll[year , , ]), axes = F, zlim = c(-0.5, 1))
    
    # datAll[year , , ]
    par("usr" = c(0, 1, 0, 1))
    seq(par("usr")[1], par("usr")[2], length.out = nRows + 1)
    abline(h = seq(par("usr")[1], par("usr")[2], length.out = nRows + 1))
    abline(v = seq(par("usr")[3], par("usr")[4], length.out = nCols + 1))
    mtext(side = 3, text = paste0("max val = ", max(datAll[year , , ])), line = -2)
    print(paste0("year = ", year))
  }
}, video.name = "twoByTwo.mp4", other.opts = "-c:v libx264 -preset ultrafast -crf 12")

dim(datAll)