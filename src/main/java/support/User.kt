package support

import enums.Customization

class User(var nicName: String,
           var phone: String,
           var pinCode: String,
           var deviceNameAndroid: String,
           var deviceNameiOS: String,
           var platformVersionAndroid: String,
           var platformVersioniOS: String,
           var customisationAndroid: Customization,
           var customisationiOS: Customization
)