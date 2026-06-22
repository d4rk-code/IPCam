#include <opencv4/opencv2/video.hpp>
#include <opencv4/opencv2/imgproc.hpp>
#include <opencv4/opencv2/highgui.hpp>

using namespace cv;
using namespace std;

class MotionDetector {
private:
    Ptr<BackgroundSubtractorMOG2> pBackSub;
    int minArea;
    
public:
    MotionDetector(int minArea = 500)
        : minArea(minArea) {
        pBackSub = createBackgroundSubtractorMOG2();
        pBackSub->setDetectShadows(false);
    }
    
    vector<Rect> detect(const Mat& frame) {
        Mat fgMask;
        pBackSub->apply(frame, fgMask);
        
        // Clean up
        Mat kernel = getStructuringElement(MORPH_ELLIPSE,
                                          Size(5, 5));
        morphologyEx(fgMask, fgMask, MORPH_OPEN, kernel);
        dilate(fgMask, fgMask, kernel);
        
        // Find contours
        vector<vector<Point>> contours;
        findContours(fgMask, contours, RETR_EXTERNAL,
                    CHAIN_APPROX_SIMPLE);
        
        // Filter and create bounding boxes
        vector<Rect> detections;
        for (const auto& cnt : contours) {
            if (contourArea(cnt) >= minArea) {
                detections.push_back(boundingRect(cnt));
            }
        }
        
        return detections;
    }
};

int main() {
	string url = "http://10.190.252.44:8000";
    VideoCapture cap(url); 
    MotionDetector detector(1000);
    
    Mat frame;
    while (cap.read(frame)) {
        auto boxes = detector.detect(frame);
        
        // Draw detections
        for (const auto& box : boxes) {
            rectangle(frame, box, Scalar(0, 255, 0), 2);
            
            string label = "Motion";
            putText(frame, label,
                   Point(box.x, box.y - 5),
                   FONT_HERSHEY_SIMPLEX, 0.5,
                   Scalar(0, 255, 0), 2);
        }
        
        // Show count
        string info = "Objects: " + to_string(boxes.size());
        putText(frame, info, Point(10, 30),
               FONT_HERSHEY_SIMPLEX, 1,
               Scalar(0, 255, 0), 2);
        
        imshow("Motion Detection", frame);
        
        if (waitKey(30) >= 0) break;
    }
    
    return 0;
}
