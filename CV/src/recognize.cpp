#include <opencv4/opencv2/video/tracking.hpp>
#include <opencv4/opencv2/highgui.hpp>
#include <opencv4/opencv2/opencv.hpp>
#include <iostream>

using namespace cv;
using namespace std;

int main() {

	string url = "http://10.210.225.44:8000";

	VideoCapture cap(url, CAP_FFMPEG);

	if(!cap.isOpened()) {
		cout << "Error while trying to capture" << endl;
		return -1;
	}

	// goal today is to do object tracking meanshift tracking

	Mat frame, hsv, backProj;
	cap >> frame;

	// selecting ROI
	Rect trackWindow = selectROI(frame);

	// calculating histogram
	Mat roi = frame(trackWindow);
    cvtColor(roi, hsv, COLOR_BGR2HSV);

	Mat hist;
	int hbins = 30;
	float hranges[] = {0, 180};
	const float* ranges[] = {hranges};
	int channels[] = {0};

	calcHist(&hsv, 1, channels, Mat(), hist,
			1, &hbins, ranges);
	normalize(hist, hist, 0, 255, NORM_MINMAX);

	while (cap.read(frame)) {
        cvtColor(frame, hsv, COLOR_BGR2HSV);
        
        // Calculate back projection
        calcBackProject(&hsv, 1, channels, hist, backProj,
                       ranges);
        
        // Apply MeanShift (tracking.hpp:107)
        TermCriteria criteria(TermCriteria::EPS | TermCriteria::COUNT,
                             10, 1);
        meanShift(backProj, trackWindow, criteria);
        
        // Draw tracking rectangle
        rectangle(frame, trackWindow, Scalar(0, 255, 0), 2);
        
        imshow("MeanShift Tracking", frame);
        if (waitKey(30) >= 0) break;
    }

    return 0;
}
