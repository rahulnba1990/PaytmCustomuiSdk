
Pod::Spec.new do |s|
  s.name         = "RNPaytmCustomuiSdk"
  s.version      = "1.0.0"
  s.summary      = "RNPaytmCustomuiSdk"
  s.description  = <<-DESC
                  This library is a react native implementation of Paytm's custom-ui SDK android/ios. For more information you can visit https://developer.paytm.com/docs/custom-ui-sdk. Current version works for Android only. Will support ios integration very soon.
                   DESC
  s.homepage     = "https://github.com/rahulnba1990/PaytmCustomuiSdk"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/rahulnba1990/RNPaytmCustomuiSdk.git", :tag => "master" }
  s.source_files  = "RNPaytmCustomuiSdk/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  