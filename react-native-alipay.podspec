require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name           = package['name']
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = package['homepage']
  s.source         = { :git => 'https://www.npmjs.com/package/react-native-alipay', :tag => s.version }

  s.requires_arc   = true
  s.platform       = :ios, '8.0'

  s.source_files   = "ios/RCTAlipay/**/*.{h,m}"

  s.frameworks     = 'SystemConfiguration', 'CoreTelephony', 'QuartzCore', 'CoreText', 'CoreGraphics', 'UIKit', 'Foundation', 'CFNetwork', 'CoreMotion'
  s.vendored_frameworks = 'ios/SDK/AlipaySDK.framework'
  
  s.dependency 'React'
end
