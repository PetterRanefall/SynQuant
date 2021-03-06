import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.text.html.HTMLDocument.Iterator;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

//Congchao's functions for 2D image handling
public class ImageHandling {
	protected int NextLabel=0;
	protected int saveImCnt = 0;
	//Image morphology, open operation, fill holes 
	public boolean [][] imMorpho(boolean[][] K1,boolean[][] kMask, int min_size){
		boolean [][] K1a = bwareaopen(K1, min_size,8);
		//IH.IMwrite(K1a, "src/res/scanAll3_a_"+ii+".png");
		boolean [][] K11a = new boolean [K1a.length][K1a[0].length];
		for(int i=1;i<K1a.length-1;i++){
			for(int j=1;j<K1a[0].length-1;j++){
				if(!K1a[i][j]){
					int myInt = (K1a[i+1][j]) ? 1 : 0;
					myInt += (K1a[i-1][j]) ? 1 : 0;
					myInt += (K1a[i][j+1]) ? 1 : 0;
					myInt += (K1a[i][j-1]) ? 1 : 0;
					if(myInt>1)
						K11a[i][j]=true;
					else
						K11a[i][j] = false;
				}
				else
					K11a[i][j] = true;
			}
		}
		for(int i=0;i<K1a.length;i+=K1a.length-1){
			for(int j=0;j<K1a[0].length;j++){
					K11a[i][j] = K1a[i][j];
			}
		}
		for(int i=0;i<K1a.length;i++){
			for(int j=0;j<K1a[0].length;j+=K1a[0].length-1){
					K11a[i][j] = K1a[i][j];
			}
		}
		//IH.IMwrite(K11a, "src/res/scanAll3_a2_"+ii+".png");
		boolean [][] K1b = imfill(K11a);
		//IH.IMwrite(K1b, "src/res/scanAll3_b_"+ii+".png");
		for(int i=0;i<K1b.length;i++)
			for(int j=0;j<K1b[0].length;j++)
				K1b[i][j] = K1b[i][j] && kMask[i][j];
		//int [][] K1b_cc = bwlabel(K1b);
		//IMwrite(K1b,"src/res/Thre_"+saveImCnt+++".png");
		return K1b;
	}
	//bwareaopen: the same as the function in matlab
	boolean [][] bwareaopen(boolean[][] BWIM, int min_size,int CONNECT){
		int [][] bwl = bwlabel(BWIM, CONNECT);
		//IH.IMwrite(K1a, "src/res/scanAll3_a_"+ii+".png");
		boolean [][] bw_out = new boolean [BWIM.length][BWIM[0].length];
        for(int i=0; i<bwl.length; i++)
            for(int j=0; j<bwl[0].length; j++)
            	bw_out[i][j]=BWIM[i][j];
		//ArrayList<Integer> reg_size = new ArrayList<Integer>();
		//long [] reg_size = new long[1000000];
		int labelnum = 0;//need to check whether region number > 65535
        for(int i=0; i<bwl.length; i++) {
            for(int j=0; j<bwl[0].length; j++) {
            	if(bwl[i][j]>labelnum)
            		labelnum = bwl[i][j];
            }
        }
        long [] reg_size = new long[labelnum];
        for(int i=0; i<bwl.length; i++) {
            for(int j=0; j<bwl[0].length; j++) {
            	if(bwl[i][j]>0)
            		reg_size[bwl[i][j]-1]++;
            }
        }
        for(int k = 1;k<labelnum+1;k++){
        	if(reg_size[k-1]<min_size){
	            for(int i=0; i<bwl.length; i++) {
	                for(int j=0; j<bwl[0].length; j++) {
	                	if(bwl[i][j]==k)
	                		bw_out[i][j]=false;
	                }
	            }	
            }
        }
        //bwl = bwlabel(BWIM);
		return bw_out;
	}
	//the same as function in imfill
	boolean [][] imfill(boolean[][] BWIM_in){
		//1-BWIM_in
		boolean[][] BWIM = new boolean [BWIM_in.length][BWIM_in[0].length];
		for(int i=0;i<BWIM.length;i++){
			for(int j=0;j<BWIM[0].length;j++){
				BWIM[i][j] = !BWIM_in[i][j];
			}
		}
		
		int [][] bwl = bwlabel(BWIM,8);
		boolean [][] bw_out = new boolean [BWIM.length][BWIM[0].length];
		ArrayList<Integer> free_reg = new ArrayList<Integer>();
		for(int i=0;i<BWIM.length;i+=BWIM.length-1){
			for(int j=0;j<BWIM[0].length;j++){
				bw_out[i][j] = BWIM_in[i][j];
				if(bwl[i][j]==0)
					continue;
				boolean saveflag = true;
				for(int k=0;k<free_reg.size();k++){
					if(bwl[i][j]==free_reg.get(k)){
						saveflag = false;
						break;
					}	
				}
				if(saveflag)
					free_reg.add(bwl[i][j]);
			}
		}
		for(int i=0;i<BWIM.length;i++){
			for(int j=0;j<BWIM[0].length;j+=BWIM[0].length-1){
				bw_out[i][j] = BWIM_in[i][j];
				if(bwl[i][j]==0)
					continue;
				boolean saveflag = true;
				for(int k=0;k<free_reg.size();k++){
					if(bwl[i][j]==free_reg.get(k)){
						saveflag = false;
						break;
					}	
				}
				if(saveflag)
					free_reg.add(bwl[i][j]);
			}
		}
		for(int i=1;i<BWIM.length-1;i++){
			for(int j=1;j<BWIM[0].length-1;j++){
				
				if(bwl[i][j]==0){
					bw_out[i][j] = true;
					continue;
				}
				boolean removeflag = true;
				for(int k=0;k<free_reg.size();k++){
					if(bwl[i][j]==free_reg.get(k)){
						removeflag = false; //the background is on edge
						break;
					}	
				}
				if(removeflag) //the background is NOT on edge
					bw_out[i][j] = true;
				else
					bw_out[i][j] = false;
					
			}
		}
		return bw_out;
	}
	//the same as bwlabel in matlab
	int [][] bwlabel(boolean[][] BWIM, int CONNECT){
        int[][] labels = new int[BWIM.length][BWIM[0].length];

        NextLabel = 1;

        //First pass
        if(CONNECT==8){
	        for(int i=0; i<BWIM.length; i++) {
	            for(int j=0; j<BWIM[0].length; j++) {
	                if(BWIM[i][j]==true & labels[i][j]==0) {
	                    //Labels of neighbors
	                	ArrayList<Integer[]> roi_pos = new ArrayList<Integer[]>();
	                	roi_pos.add(new Integer[] {i,j});
	                	labels[i][j]=NextLabel;
	                	int loop_cnt = 0;
	                	while(true){
	                		if(loop_cnt>=roi_pos.size())
	                			break;
	                		//System.out.println(""+loop_cnt);
	                		//if(loop_cnt==443)
	                		//	loop_cnt = 443;
	                		int cur_i = roi_pos.get(loop_cnt)[0];
	                		int cur_j = roi_pos.get(loop_cnt++)[1];
	                		for(int ni=-1; ni<=1; ni++) {
	                            for(int nj=-1; nj<=1; nj++) {
	                                if(cur_i+ni<0 || cur_j+nj<0 || cur_i+ni>labels.length-1 || cur_j+nj>labels[0].length-1) {
	                                    continue;
	                                }
	                                else {
	                                    if(ni == 0 && nj == 0) continue;
	                                    if(labels[cur_i+ni][cur_j+nj] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i+ni][cur_j+nj]){
	                                    	roi_pos.add(new Integer[] {cur_i+ni,cur_j+nj});
	                                    	labels[cur_i+ni][cur_j+nj] = NextLabel;
	                                    }
	                                }
	                            }
	                		}
	                		//deeplabel(BWIM, labels, i, j ,NextLabel);
	                	}
	                	NextLabel++;
	                }
	            }
	        }
        }
        else{
	        for(int i=0; i<BWIM.length; i++) {
	            for(int j=0; j<BWIM[0].length; j++) {
	                if(BWIM[i][j]==true & labels[i][j]==0) {
	                    //Labels of neighbors
	                	ArrayList<Integer[]> roi_pos = new ArrayList<Integer[]>();
	                	roi_pos.add(new Integer[] {i,j});
	                	labels[i][j]=NextLabel;
	                	int loop_cnt = 0;
	                	while(true){
	                		if(loop_cnt>=roi_pos.size())
	                			break;
	                		//System.out.println(""+loop_cnt);
	                		//if(loop_cnt==443)
	                		//	loop_cnt = 443;
	                		int cur_i = roi_pos.get(loop_cnt)[0];
	                		int cur_j = roi_pos.get(loop_cnt++)[1];
	                		if(cur_i+1<BWIM.length){
	                            if(labels[cur_i+1][cur_j] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i+1][cur_j]){
	                            	roi_pos.add(new Integer[] {cur_i+1,cur_j});
	                            	labels[cur_i+1][cur_j] = NextLabel;
	                            }
	                		}
	                		if(cur_j+1<BWIM[0].length){
	                            if(labels[cur_i][cur_j+1] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i][cur_j+1]){
	                            	roi_pos.add(new Integer[] {cur_i,cur_j+1});
	                            	labels[cur_i][cur_j+1] = NextLabel;
	                            }
	                		}
	                		if(cur_i-1>0){
	                            if(labels[cur_i-1][cur_j] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i-1][cur_j]){
	                            	roi_pos.add(new Integer[] {cur_i-1,cur_j});
	                            	labels[cur_i-1][cur_j] = NextLabel;
	                            }
	                		}
	                		if(cur_j-1>0){
	                            if(labels[cur_i][cur_j-1] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i][cur_j-1]){
	                            	roi_pos.add(new Integer[] {cur_i,cur_j-1});
	                            	labels[cur_i][cur_j-1] = NextLabel;
	                            }
	                		}
	                		//deeplabel(BWIM, labels, i, j ,NextLabel);
	                	}
	                	NextLabel++;
	                }
	            }
	        }
        }
        NextLabel--;
        return labels;
	}
    void deeplabel(boolean[][] matrix, int[][] labels, int i, int j ,int NextLabel){
		for(int ni=-1; ni<=1; ni++) {
            for(int nj=-1; nj<=1; nj++) {
                if(i+ni<0 || j+nj<0 || i+ni>labels.length-1 || j+nj>labels[0].length-1) {
                    continue;
                }
                else {
                    if(ni == 0 && nj == 0) continue;
                    if(labels[i+ni][j+nj] == 0 & matrix[i][j] == matrix[i+ni][j+nj]){
                    	labels[i+ni][j+nj] = NextLabel;
                    	deeplabel(matrix, labels, i+ni, j+nj ,NextLabel);
                    }
                }
            }
		}
}
    //score one region with several constrains in tmask, bmask
	double scanOneSyn(double[][] d/*Gt*/, boolean[][] tmask/*binary map>thr*/, boolean[][] bmask/*detected synapses=0*/, int[][] reg0, ParaP p, ParaQ q) {
		// TODO Auto-generated method stub
		return getOrderStatUnBalanced( d, reg0, bmask, tmask, q.var, q.ntry, p.mu, p.sigma, p.test );
	}
	double getOrderStatUnBalanced(double[][] d, int[][] reg0,boolean[][] bmask, boolean[][] tmask, double qVar,int qNtry,double[][] pMu,double[][] pSigma,String pTest ){
		
		double [] g1 = new double[reg0.length];
		int nPix = reg0.length;
		boolean[][] tmask1 = new boolean [tmask.length][tmask[0].length];
		for(int i=0;i<tmask.length;i++){
			for(int j=0;j<tmask[0].length;j++){
				tmask1[i][j] = tmask[i][j];
			}
		}
		int[][] smask = new int [tmask.length][tmask[0].length];//synapse candidate mask
		ArrayList<Integer[]>RegCoor = new ArrayList<Integer[]>();
		for(int i=0;i<reg0.length;i++){
			tmask1[reg0[i][0]][reg0[i][1]] = false; //tmask1 is the foreground mask(now remove the candidate first), got by thresholding thr0:thr1
			smask[reg0[i][0]][reg0[i][1]] = 1;
			RegCoor.add(new Integer[] {reg0[i][0], reg0[i][1]});
			g1[i] = d[reg0[i][0]][reg0[i][1]];
		}

		boolean done = false;
		int ntry = 1;
		int nNeibPrev = 0;
		ArrayList<Integer[]>NeibCoor = new ArrayList<Integer[]>();
		double [] g2 = new double[1];
		while(!done){
			for(int i=0;i<RegCoor.size();i++){
				for(int hs = -1;hs<2;hs++){
					for(int vs = -1;vs<2;vs++){
						/*if(hs==0 && vs==0) //the point itself needs to be tested too
							continue;*/ 
						int tmp_y = vs+RegCoor.get(i)[0];
						int tmp_x = hs+RegCoor.get(i)[1];
						if(tmp_y>=tmask.length || tmp_x>=tmask[0].length || tmp_y<0 || tmp_x<0)
							continue;
						if(smask[tmp_y][tmp_x]==0 && bmask[tmp_y][tmp_x] && !tmask1[tmp_y][tmp_x]){
							NeibCoor.add(new Integer[] {tmp_y, tmp_x});
						}
					}
				}
			}
			ArrayList<Integer[]> uniqueNeiCoor = new ArrayList<Integer[]>();
			if(NeibCoor.size()>0)
				uniqueNeiCoor.add(NeibCoor.get(0));
			for(int i=1;i<NeibCoor.size();i++){
				boolean add_flag = true;
				for(int j=0;j<uniqueNeiCoor.size();j++){
					if(uniqueNeiCoor.get(j)[0]==NeibCoor.get(i)[0] && uniqueNeiCoor.get(j)[1]==NeibCoor.get(i)[1]){
						add_flag = false;
						break;
					}
				}
				if(add_flag){
					uniqueNeiCoor.add(NeibCoor.get(i));
				}
			}
			int nNeib = uniqueNeiCoor.size();
			if(nNeib>=nPix || nNeibPrev==nNeib || ntry==qNtry){
				g2 = new double [nNeib];
				java.util.Iterator<Integer[]> it = uniqueNeiCoor.iterator();
				int g2_cnt = 0;
				while(it.hasNext()){
					//System.out.println(""+d[it.next()[0]][it.next()[1]]);
					Integer[] tmp_it = it.next();
					g2[g2_cnt++]=d[tmp_it[0]][tmp_it[1]];
					//
				}
				done = true;
			}
			else {
				java.util.Iterator<Integer[]> it = NeibCoor.iterator();
				while(it.hasNext()){
					RegCoor.add(it.next());
				}
			}
			ntry++;
			nNeibPrev = nNeib;
		}
		//test statistics: g2 save the neighboring pixel coordinates, g1 is the pixels inside ROI
		BasicMath mBM = new BasicMath();
	    double t0 = mBM.sampleMean(g1)-mBM.sampleMean(g2);
	    int M = g1.length;
	    int N = g2.length;
	    if(M < 4)
	        M = 4;
	    
	    if(N<=3 || N < (M/10))
	        return 0;
	    if(M>100)
	        M = 100;
	    if(N>100)
	        N = 99;
	    double mu = pMu[M-1][N-1];
	    double sigma = pSigma[M-1][N-1];
	    mu = mu*Math.sqrt(qVar);
	    sigma = sigma*Math.sqrt(qVar);

	    double zScore = (t0-mu)/sigma;
	    t0 = t0/Math.sqrt(qVar);
		
		return zScore;
	}
	//output for double matrix
	public void IMwrite(double[][] imarray, String filepath){
	///////////////////// 	  Output intermediate result image (for inspection)     ///////////////////////

		int[][] showImg = new int[imarray.length][imarray[0].length];
		int numRows = showImg.length;
		int numCols = showImg[0].length;
		for (int x = 0; x < numCols; x++) {
			for (int y = 0; y < numRows; y++) {
				if(imarray[y][x]<0)
					showImg[y][x] = 0;
				else{
					showImg[y][x] = (int)(imarray[y][x]);
					}
			}
		}

		int pixel;
		BufferedImage bufferedImage = new BufferedImage(numCols, numRows, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster raster = bufferedImage.getRaster();
		for (int x = 0; x < numCols; x++) {
			for (int y = 0; y < numRows; y++) {
				pixel = showImg[y][x];
				//pixel =  0 + (pixel << 8)  + (0 <<16);
				//		            System.out.println("The pixel in Matrix: "+pixel);
				raster.setSample(x, y, 0, pixel);
				//		            System.out.println("The pixel in BufferedImage: "+bufferedImage.getRGB(x, y));
			}
		}
		System.out.println("BufferedImage got.");

		File outputfile = new File(filepath);
		try {
			ImageIO.write(bufferedImage, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("BufferedImage saved.");

	//////////////////////////////////////////////////////////////////////////////////////////////////////
}
	//output for bool matrix
	public void IMwrite(boolean[][] imarray, String filepath){
	///////////////////// 	  Output intermediate result image (for inspection)     ///////////////////////

		int[][] showImg = new int[imarray.length][imarray[0].length];
		int numRows = showImg.length;
		int numCols = showImg[0].length;
		for (int x = 0; x < numCols; x++) {
			for (int y = 0; y < numRows; y++) {
				if(imarray[y][x])
					showImg[y][x] = 255;
				else
					showImg[y][x] = 0;
			}
		}

		int pixel;
		BufferedImage bufferedImage = new BufferedImage(numCols, numRows, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < numCols; x++) {
			for (int y = 0; y < numRows; y++) {
				pixel = showImg[y][x];
				pixel =  pixel + (pixel << 8)  + (pixel <<16);
				//		            System.out.println("The pixel in Matrix: "+pixel);
				bufferedImage.setRGB(x, y, pixel);
				//		            System.out.println("The pixel in BufferedImage: "+bufferedImage.getRGB(x, y));
			}
		}
		System.out.println("BufferedImage got.");

		File outputfile = new File(filepath);
		try {
			ImageIO.write(bufferedImage, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("BufferedImage saved.");

	//////////////////////////////////////////////////////////////////////////////////////////////////////
}
	//output for int matrix
	public void IMwrite(int[][] imarray, String filepath){
	///////////////////// 	  Output intermediate result image (for inspection)     ///////////////////////

		int[][] showImg = new int[imarray.length][imarray[0].length];
		int numRows = showImg.length;
		int numCols = showImg[0].length;
		for (int x = 0; x < numCols; x++) {
			for (int y = 0; y < numRows; y++) {
				if(imarray[y][x]>255)
					showImg[y][x] = 0;
				else
					showImg[y][x] = imarray[y][x];
			}
		}

		int pixel;
		BufferedImage bufferedImage = new BufferedImage(numCols, numRows, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < numCols; x++) {
			for (int y = 0; y < numRows; y++) {
				pixel = showImg[y][x];
				pixel =  pixel + (pixel << 8)  + (pixel <<16);
				//		            System.out.println("The pixel in Matrix: "+pixel);
				bufferedImage.setRGB(x, y, pixel);
				//		            System.out.println("The pixel in BufferedImage: "+bufferedImage.getRGB(x, y));
			}
		}
		System.out.println("BufferedImage got.");

		File outputfile = new File(filepath);
		try {
			ImageIO.write(bufferedImage, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("BufferedImage saved.");

	//////////////////////////////////////////////////////////////////////////////////////////////////////
}
//For grow neurite
	boolean[][] imopen(boolean[][] inputIm, int connect){
		boolean[][] outIm = new boolean[inputIm.length][inputIm[0].length];
		if(connect==4)
			for(int y =0;y<inputIm.length;y++){
				for(int x=0;x<inputIm[0].length;x++){
					if(inputIm[y][x]){
						outIm[y][x] = true;
						if(y>0)
							outIm[y-1][x] = true;
						if(x>0)
							outIm[y][x-1] = true;
						if(y<inputIm.length-1)
							outIm[y+1][x] = true;
						if(x<inputIm[0].length-1)
							outIm[y][x+1] = true;
					}
					else
						outIm[y][x] = false;
				}
			}
		if(connect==8)
			for(int y =0;y<inputIm.length;y++){
				for(int x=0;x<inputIm[0].length;x++){
					if(inputIm[y][x]){
						outIm[y][x] = true;
						if(y>0)
							outIm[y-1][x] = true;
						if(x>0)
							outIm[y][x-1] = true;
						if(y<inputIm.length-1)
							outIm[y+1][x] = true;
						if(x<inputIm[0].length-1)
							outIm[y][x+1] = true;
						
						if(y>0 & x>0)
							outIm[y-1][x-1] = true;
						if(y>0 & x<inputIm[0].length-1)
							outIm[y-1][x+1] = true;
						if(y<inputIm.length-1 & x>0)
							outIm[y+1][x-1] = true;
						if(y<inputIm.length-1 & x<inputIm[0].length-1)
							outIm[y+1][x+1] = true;
					}
					else
						outIm[y][x] = false;
				}
			}
		return outIm;
	}
	boolean[][] Skeleton(boolean[][] inIm){
		
		ByteProcessor outIP =  new ByteProcessor(inIm[0].length, inIm.length);
		
		for (int i = 0; i < inIm[0].length; i++) {
			for(int j = 0;j<inIm.length;j++){
			// put channel values in an integer
			if(inIm[j][i])
				outIP.set(i, j, 0);
			else
				outIP.set(i, j, 255);
			}
		}
		BinaryProcessor bi_IP = new BinaryProcessor(outIP);
		bi_IP.skeletonize();
		boolean[][] outIm = new boolean[inIm.length][inIm[0].length]; 
		for (int i = 0; i < inIm[0].length; i++) {
			for(int j = 0;j<inIm.length;j++){
			if(bi_IP.get(i, j)>0)
				outIm[j][i] = false;
			else
				outIm[j][i] = true;
			}
		}
		return outIm;
	}
	ArrayList<Integer[]> branchpts(boolean[][] skel){
		ArrayList<Integer[]> branchPts = new ArrayList<Integer[]>();
		for(int y=0;y<skel.length;y++){
			for(int x=0;x<skel[0].length;x++){
    			
				if(!skel[y][x])
    				continue;
    			int neiCnt = 0;
        		for(int ni=-1; ni<=1; ni++) {
                    for(int nj=-1; nj<=1; nj++) {
                        if(x+ni<0 || y+nj<0 || x+ni>skel[0].length-1 || y+nj>skel.length-1) 
                            continue;
                        else{
                        	if(ni == 0 && nj == 0) continue;
                        	if(skel[y+nj][x+ni]) neiCnt++;
                        }
                        }
                    }
        		if(neiCnt>2)
        			branchPts.add(new Integer[] {y,x});
        		
			}
		}
		return branchPts;
	}
	ArrayList<Integer[]> endpts(boolean[][] skel){
		ArrayList<Integer[]> endPts = new ArrayList<Integer[]>();
		for(int y=0;y<skel.length;y++){
			for(int x=0;x<skel[0].length;x++){
    			
				if(!skel[y][x])
    				continue;
    			int neiCnt = 0;
        		for(int ni=-1; ni<=1; ni++) {
                    for(int nj=-1; nj<=1; nj++) {
                        if(x+ni<0 || y+nj<0 || x+ni>skel[0].length-1 || y+nj>skel.length-1) 
                            continue;
                        else{
                        	if(ni == 0 && nj == 0) continue;
                        	if(skel[y+nj][x+ni]) neiCnt++;
                        }
                        }
                    }
        		if(neiCnt==1)
        			endPts.add(new Integer[] {y,x});
        		
			}
		}
		return endPts;
	}
	ArrayList<Integer[]> branchendpts(boolean[][] skel){
		ArrayList<Integer[]> endPts = new ArrayList<Integer[]>();
		for(int y=0;y<skel.length;y++){
			for(int x=0;x<skel[0].length;x++){
    			
				if(!skel[y][x])
    				continue;
    			int neiCnt = 0;
        		for(int ni=-1; ni<=1; ni++) {
                    for(int nj=-1; nj<=1; nj++) {
                        if(x+ni<0 || y+nj<0 || x+ni>skel[0].length-1 || y+nj>skel.length-1) 
                            continue;
                        else{
                        	if(ni == 0 && nj == 0) continue;
                        	if(skel[y+nj][x+ni]) neiCnt++;
                        }
                        }
                    }
        		if(neiCnt==1 || neiCnt>2)
        			endPts.add(new Integer[] {y,x});
        		
			}
		}
		return endPts;
	}
	//bwlabel with regularization
	public int[][] Regbwlabel(boolean[][] BWIM, int CONNECT, int regularization) {
        int[][] labels = new int[BWIM.length][BWIM[0].length];

        NextLabel = 1;

        //First pass
        if(CONNECT==8){
	        for(int i=0; i<BWIM.length; i++) {
	            for(int j=0; j<BWIM[0].length; j++) {
	                if(BWIM[i][j]==true & labels[i][j]==0) {
	                    //Labels of neighbors
	                	ArrayList<Integer[]> roi_pos = new ArrayList<Integer[]>();
	                	roi_pos.add(new Integer[] {i,j});
	                	labels[i][j]=NextLabel;
	                	int loop_cnt = 0;
	                	while(true){
	                		if(roi_pos.size()>regularization || loop_cnt>=roi_pos.size())
	                			break;

	                		int cur_i = roi_pos.get(loop_cnt)[0];
	                		int cur_j = roi_pos.get(loop_cnt++)[1];
	                		for(int ni=-1; ni<=1; ni++) {
	                            for(int nj=-1; nj<=1; nj++) {
	                                if(cur_i+ni<0 || cur_j+nj<0 || cur_i+ni>labels.length-1 || cur_j+nj>labels[0].length-1) {
	                                    continue;
	                                }
	                                else {
	                                    if(ni == 0 && nj == 0) continue;
	                                    if(labels[cur_i+ni][cur_j+nj] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i+ni][cur_j+nj]){
	                                    	roi_pos.add(new Integer[] {cur_i+ni,cur_j+nj});
	                                    	labels[cur_i+ni][cur_j+nj] = NextLabel;
	                                    }
	                                }
	                            }
	                		}
	                	}
	                	NextLabel++;
	                }
	            }
	        }
        }
        else{
	        for(int i=0; i<BWIM.length; i++) {
	            for(int j=0; j<BWIM[0].length; j++) {
	                if(BWIM[i][j]==true & labels[i][j]==0) {
	                    //Labels of neighbors
	                	ArrayList<Integer[]> roi_pos = new ArrayList<Integer[]>();
	                	roi_pos.add(new Integer[] {i,j});
	                	labels[i][j]=NextLabel;
	                	int loop_cnt = 0;
	                	while(true){
	                		if(roi_pos.size()>regularization || loop_cnt>=roi_pos.size())
	                			break;
	                		//System.out.println(""+loop_cnt);
	                		//if(loop_cnt==443)
	                		//	loop_cnt = 443;
	                		int cur_i = roi_pos.get(loop_cnt)[0];
	                		int cur_j = roi_pos.get(loop_cnt++)[1];
	                		if(cur_i+1<BWIM.length){
	                            if(labels[cur_i+1][cur_j] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i+1][cur_j]){
	                            	roi_pos.add(new Integer[] {cur_i+1,cur_j});
	                            	labels[cur_i+1][cur_j] = NextLabel;
	                            }
	                		}
	                		if(cur_j+1<BWIM[0].length){
	                            if(labels[cur_i][cur_j+1] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i][cur_j+1]){
	                            	roi_pos.add(new Integer[] {cur_i,cur_j+1});
	                            	labels[cur_i][cur_j+1] = NextLabel;
	                            }
	                		}
	                		if(cur_i-1>0){
	                            if(labels[cur_i-1][cur_j] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i-1][cur_j]){
	                            	roi_pos.add(new Integer[] {cur_i-1,cur_j});
	                            	labels[cur_i-1][cur_j] = NextLabel;
	                            }
	                		}
	                		if(cur_j-1>0){
	                            if(labels[cur_i][cur_j-1] == 0 & BWIM[cur_i][cur_j] == BWIM[cur_i][cur_j-1]){
	                            	roi_pos.add(new Integer[] {cur_i,cur_j-1});
	                            	labels[cur_i][cur_j-1] = NextLabel;
	                            }
	                		}
	                		//deeplabel(BWIM, labels, i, j ,NextLabel);
	                	}
	                	NextLabel++;
	                }
	            }
	        }
        }
        NextLabel--;
        return labels;
	}
}
