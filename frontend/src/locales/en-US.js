export default {
  common: {
    profile: 'Profile',
    logout: 'Logout',
    login: 'Login',
    register: 'Register',
    search: 'Search',
    searchPlaceholder: 'Search for content...',
    viewAll: 'View All',
    confirm: 'Confirm',
    cancel: 'Cancel',
    save: 'Save',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    submit: 'Submit',
    reset: 'Reset',
    back: 'Back',
    next: 'Next',
    prev: 'Previous',
    loading: 'Loading...',
    noData: 'No Data',
    operation: 'Operation',
    status: 'Status',
    createTime: 'Create Time',
    updateTime: 'Update Time',
    actions: 'Actions',
    success: 'Success',
    failed: 'Failed',
    warning: 'Warning',
    info: 'Info',
    error: 'Error',
    close: 'Close'
  },
  menu: {
    dashboard: 'Dashboard',
    shop: 'Shop Management',
    menu: 'Menu Management',
    menuDeveloping: 'Menu Management Under Development',
    rooms: 'Room Type Management',
    roomCalendar: 'Room Status Management',
    orders: 'Order Management',
    reviews: 'Review Management',
    qualification: 'Qualification Application',

    statistics: 'Statistics',
    activities: 'Activity Management',
    home: 'Home',
    recommend: 'Recommendations',
    ceramicExperience: 'Ceramic Experience',
    plan: 'Trip Planning',
    personalCenter: 'Personal Center',
    merchantCenter: 'Merchant Center',
    adminCenter: 'Admin Center'
  },
  merchant: {
    title: {
      food: 'Food Merchant Center',
      hotel: 'Hotel Merchant Center',
      ceramic: 'Ceramic Merchant Center',
      scenic: 'Scenic Merchant Center',
      default: 'Merchant Center'
    },
    category: {
      food: 'Food Merchant',
      hotel: 'Hotel Merchant',
      ceramic: 'Ceramic Workshop',
      scenic: 'Scenic Merchant',
      default: 'General Merchant'
    },
    welcome: {
      morning: 'Good Morning',
      noon: 'Good Noon',
      afternoon: 'Good Afternoon',
      evening: 'Good Evening',
      night: 'Good Night',
      dawn: 'Good Dawn',
      food: 'Ready to serve diners?',
      hotel: 'Ready to welcome guests?',
      ceramic: 'Ready to showcase ceramic art?',
      scenic: 'Ready to welcome tourists?',
      default: 'Wishing you prosperity!',
      foodMessage: 'View the latest food orders and reviews, manage your menu.',
      hotelMessage: 'View the latest bookings and check-ins, manage your room types.',
      ceramicMessage: 'View the latest experience bookings and courses, manage your workshop.',
      scenicMessage: 'View the latest ticket bookings and tourist feedback, manage your scenic spot.',
      defaultMessage: 'View the latest orders and reviews, manage your shop.'
    },
    quickActions: {
      addProduct: 'Add New Product',
      handleOrders: 'Handle Orders',
      replyReviews: 'Reply to Reviews',
      promotion: 'Marketing'
    },
    stats: {
      totalOrders: 'Total Orders',
      totalRevenue: 'Total Revenue',
      averageRating: 'Average Rating',
      totalViews: 'Total Views',
      ordersIncrease: 'New orders since yesterday',
      orders: 'orders',
      revenueIncrease: 'Increase since yesterday',
      totalReviews: 'Total',
      reviews: 'reviews',
      viewsIncrease: 'New views since yesterday',
      views: 'views'
    },
    dashboard: {
      dataTrend: 'Data Trend',
      last7Days: 'Last 7 Days',
      last30Days: 'Last 30 Days',
      orders: 'Orders',
      revenue: 'Revenue',
      todoList: 'Todo List',
      systemNotice: 'System Notice',
      recentOrders: 'Recent Orders',
      orderNumber: 'Order Number',
      customer: 'Customer',
      project: {
        ceramic: 'Experience Project',
        food: 'Dish/Service',
        hotel: 'Room Type',
        default: 'Service Project'
      },
      amount: 'Amount',
      orderTime: 'Order Time',
      monday: 'Mon',
      tuesday: 'Tue',
      wednesday: 'Wed',
      thursday: 'Thu',
      friday: 'Fri',
      saturday: 'Sat',
      sunday: 'Sun'
    },
    todo: {
      confirmNewOrders: 'Confirm New Orders',
      replyBadReviews: 'Reply to Bad Reviews',
      updateInventory: 'Update Room Status/Inventory',
      urgent: 'Urgent',
      important: 'Important',
      normal: 'Normal'
    },
    notice: {
      activity: 'Activity',
      system: 'System',
      springPromotion: 'Spring Festival Promotion Registration Open',
      systemUpgrade: 'Merchant Backend Feature Upgrade Notice'
    },
    orderStatus: {
      pending: 'Pending',
      confirmed: 'Confirmed',
      cancelled: 'Cancelled'
    },
    weather: {
      sunny: 'Sunny today, 24℃'
    },
    developing: 'Feature under development, stay tuned'
  },
  header: {
    welcome: 'Welcome to Jingdezhen!',
    role: {
      admin: 'Admin',
      merchant: 'Merchant',
      user: 'User',
      guest: 'Guest'
    }
  },
  footer: {
    systemName: 'Jingdezhen Tourism Intelligent Recommendation System',
    slogan: 'Explore the Millennium Porcelain Capital, Experience Ceramic Culture',
    touristServices: 'Tourist Services',
    scenicBrowse: 'Scenic Spots',
    recommend: 'Recommendations',
    plan: 'Trip Planning',
    merchantServices: 'Merchant Services',
    merchantCenter: 'Merchant Center',
    shopManage: 'Shop Management',
    orderManage: 'Order Management',
    aboutUs: 'About Us',
    contactUs: 'Contact Us',
    helpCenter: 'Help Center',
    feedback: 'Feedback',
    copyright: 'All rights reserved.'
  },
  language: {
    chinese: '简体中文',
    english: 'English'
  },
  auth: {
    login: {
      title: 'Login',
      welcome: 'Welcome back to Jingdezhen Tourism Intelligent Recommendation System',
      username: 'Username',
      password: 'Password',
      remember: 'Remember me',
      forgotPassword: 'Forgot password?',
      loginButton: 'Login',
      noAccount: 'No account yet?',
      registerNow: 'Register now',
      loginSuccess: 'Login successful',
      loginFailed: 'Login failed, please check username and password',
      networkError: 'Network connection failed, please check network settings',
      loginFailedRetry: 'Login failed, please try again later',
      adminLoginFailed: 'Admin login failed. Please check:\n1. Is the password correct (default password may be 123456)\n2. Is the backend service running normally\n3. Does the admin account exist in the database',
      banner: {
        title: 'Explore the Millennium Porcelain Capital',
        subtitle: 'Experience ceramic culture and feel the charm of traditional craftsmanship',
        feature1: 'Intelligent Recommendations',
        feature2: 'Trip Planning',
        feature3: 'Scenic Navigation'
      },
      validation: {
        usernameRequired: 'Please enter username',
        passwordRequired: 'Please enter password',
        passwordMinLength: 'Password must be at least 6 characters'
      }
    },
    register: {
      title: 'Register',
      welcome: 'Join us and start your Jingdezhen journey',
      username: 'Username',
      password: 'Password',
      confirmPassword: 'Confirm Password',
      phone: 'Phone Number',
      selectRole: 'Select Role',
      selectMerchantCategory: 'Please select merchant category',
      tourist: 'Tourist',
      merchant: 'Merchant',
      foodMerchant: 'Food Merchant',
      ceramicWorkshop: 'Ceramic Workshop',
      hotelMerchant: 'Hotel Merchant',
      scenicMerchant: 'Scenic Merchant',
      other: 'Other',
      agreement: 'I have read and agree to',
      userAgreement: 'User Agreement',
      and: 'and',
      privacyPolicy: 'Privacy Policy',
      registerButton: 'Register',
      hasAccount: 'Already have an account?',
      loginNow: 'Login now',
      registerSuccess: 'Registration successful, please login',
      banner: {
        title: 'Welcome to Jingdezhen',
        subtitle: 'Millennium Porcelain Capital, the birthplace of ceramic culture',
        feature1: 'Personalized Recommendations',
        feature2: 'Intelligent Trip Planning',
        feature3: 'Detailed Scenic Information',
        feature4: 'User Reviews and Communication'
      },
      agreementDialog: {
        title: 'User Agreement',
        content: {
          title: 'User Agreement',
          p1: 'Welcome to Jingdezhen Tourism Intelligent Recommendation System!',
          p2: '1. Users should comply with relevant laws and regulations when using this system.',
          p3: '2. Users should ensure the authenticity and accuracy of the information provided.',
          p4: '3. Users shall not use this system for illegal activities.',
          p5: '4. This system reserves the right to handle users who violate the rules.'
        }
      },
      privacyDialog: {
        title: 'Privacy Policy',
        content: {
          title: 'Privacy Policy',
          p1: 'We value your privacy protection:',
          p2: '1. We only collect necessary user information to provide services.',
          p3: '2. Your personal information will not be disclosed to third parties.',
          p4: '3. You can view and modify personal information at any time.',
          p5: '4. We adopt security measures to protect your data security.'
        }
      },
      validation: {
        usernameRequired: 'Please enter username',
        usernameLength: 'Username must be between 3 and 20 characters',
        passwordRequired: 'Please enter password',
        passwordMinLength: 'Password must be at least 6 characters',
        confirmPasswordRequired: 'Please confirm password',
        passwordMismatch: 'The two passwords do not match',
        phoneRequired: 'Please enter phone number',
        phoneInvalid: 'Please enter a valid phone number',
        roleRequired: 'Please select role',
        merchantCategoryRequired: 'Please select merchant category',
        agreementRequired: 'Please read and agree to the user agreement'
      }
    },
    forgotPassword: {
      title: 'Forgot Password',
      welcome: 'Verify your identity with username and registered phone number to reset password',
      username: 'Username',
      phone: 'Registered phone number',
      newPassword: 'New Password',
      confirmPassword: 'Confirm New Password',
      resetButton: 'Reset Password',
      rememberPassword: 'Remember your password?',
      backToLogin: 'Back to Login',
      resetSuccess: 'Password reset successfully, please login with new password',
      resetFailed: 'Password reset failed',
      banner: {
        title: 'Secure Reset',
        subtitle: 'Set a new password after identity verification',
        feature1: 'Username + Phone Verification',
        feature2: 'No SMS Code Required',
        feature3: 'Quick Account Recovery'
      },
      validation: {
        usernameRequired: 'Please enter username',
        phoneRequired: 'Please enter phone number',
        phoneInvalid: 'Please enter a valid 11-digit phone number',
        passwordRequired: 'Please enter new password',
        passwordMinLength: 'Password must be at least 6 characters',
        confirmPasswordRequired: 'Please confirm new password',
        passwordMismatch: 'The two passwords do not match'
      }
    }
  },
  notFound: {
    title: 'Page Under Development',
    description: 'Sorry, the page you are visiting is under development',
    goHome: 'Go Home',
    goBack: 'Go Back'
  },
  card: {
    favorite: 'Favorite',
    favorited: 'Favorited',
    unfavorite: 'Unfavorite',
    addToPlan: 'Add to Plan',
    viewDetail: 'View Details',
    qualificationApproved: 'Qualification Approved',
    promotion: 'Promotion',
    free: 'Free',
    reviews: 'reviews',
    noImage: 'No Image'
  },
  detail: {
    notFound: 'Information not found',
    loadingFailed: 'Failed to load',
    address: 'Address',
    phone: 'Phone',
    openTime: 'Opening Hours',
    noAddress: 'No address information',
    noPhone: 'No phone number',
    noOpenTime: 'No opening hours',
    noDescription: 'No detailed description',
    introduction: 'Introduction',
    merchantInfo: 'Merchant Information',
    merchantActivities: 'Merchant Activities',
    active: 'Active',
    ended: 'Ended',
    userReviews: 'User Reviews',
    addToPlan: 'Add to Plan',
    share: 'Share',
    back: 'Back',
    open: 'Open',
    closed: 'Closed',
    location: 'Location Navigation',
    baiduMap: 'Baidu Map',
    amap: 'Amap',
    qqMap: 'Tencent Map',
    perPerson: 'Per Person',
    cuisineType: 'Cuisine Type',
    merchantType: 'Merchant Type',
    noPrice: 'No price information',
    person: 'person',
    yuan: 'yuan',
    food: 'Food',
    foodDescription: 'Quality food merchants providing delicious meals for you.',
    merchantInfoText: 'The store is provided by platform merchants. You can consult or make reservations by phone.',
    reviewPlaceholder: 'Share your dining experience...',
    publishReview: 'Publish Review',
    loginToReview: 'Please login before posting a review',
    anonymous: 'Anonymous',
    merchantReply: 'Merchant Reply',
    noReviews: 'No reviews yet',
    startTime: 'Start Time: ',
    endTime: 'End Time: ',
    reply: 'Reply',
    workshopReviewPlaceholder: 'Share your ceramic experience...',
    bookNow: 'Book Now',
    hotelDescription: 'Quality merchant, providing comfortable service.',
    activeCount: '{count} Active',
    roomType: 'Room Types',
    starLevel: 'Star Level',
    selectRoomType: 'Select Room Type',
    noRoomType: 'No available room types',
    checkInDate: 'Check-in Date',
    checkOutDate: 'Check-out Date',
    roomQuantity: 'Room Quantity',
    contactName: 'Contact Name',
    note: 'Note',
    enterContactName: 'Enter contact name',
    enterNote: 'Special requests',
    estimatedTotal: 'Estimated Total',
    to: 'to',
    ticketPrice: 'Ticket Price: ',
    scenicIntroduction: 'Scenic Introduction',
    promotion: 'Promotions',
    previewImage: 'Preview Image',
    thumbnail: 'Thumbnail',
    image: 'Image',
    viewMap: 'View Map',
    contactMerchant: 'Contact Merchant',
    bookingNotes: 'Booking Notes',
    bookingTip1: 'Please book in advance to avoid delays.',
    bookingTip2: 'Please contact merchant in advance for changes or cancellations.',
    bookingTip3: 'Please bring valid ID for check-in.',
    replyTo: 'Reply to @',
    locationInfo: 'Location Info',
    mapLoading: 'Map Loading...',
    viewInMap: 'View in Map',
    getDirections: 'Get Directions',
    actions: 'Actions',
    contactInfo: 'Contact Info',
    features: 'Features',
    highlights: 'Highlights',
    facilities: 'Facilities',
    allDay: 'Open all day',
    defaultAddress: 'Jingdezhen City',
    category: 'Category',
    tips: 'Travel Tips',
    to: 'to',
    ticketPrice: 'Ticket Price: ',
    scenicIntroduction: 'Scenic Introduction',
    promotion: 'Promotions',
    previewImage: 'Preview Image',
    thumbnail: 'Thumbnail',
    image: 'Image',
    loginToBook: 'Please login to book',
    selectDate: 'Please select check-in date',
    bookingSuccess: 'Booking successful!',
    bookingFailed: 'Booking failed',
    loginToAddToPlan: 'Please login to add to plan',
    copySuccess: 'Link copied to clipboard',
    copyFailed: 'Copy failed, please copy manually',
    callMerchant: 'Please call merchant: {phone}',
    missingId: 'Missing ID',
    loadFailed: 'Failed to load details',
    enterContent: 'Please enter content',
    noMerchantInfo: 'Cannot get merchant info',
    reviewSuccess: 'Review published successfully',
    reviewFailed: 'Failed to publish review',
    loginToReply: 'Please login to reply',
    replySuccess: 'Reply successful',
    replyFailed: 'Reply failed'
  },
  ceramic: {
    marketplace: {
      title: 'Ceramic Marketplace Crafts',
      subtitle: 'Explore major ceramic marketplaces in Jingdezhen and discover exquisite crafts',
      routeTitle: 'One-Day Marketplace Tour',
      routeSub: 'A ceramic walking route from traditional to modern',
      viewDetail: 'View Details',
      crafts: 'Featured Crafts',
      introduction: 'Marketplace Introduction',
      location: 'Location Navigation'
    },
    workshop: {
      title: 'Ceramic DIY Workshop',
      subtitle: 'Create ceramic works with your own hands and experience the fun of traditional ceramic craftsmanship',
      about: 'About Ceramic DIY Workshop',
      aboutDesc: 'Here, clay is no longer a cold material, but warm art in your hands. We provide you with complete professional ceramic tools and guidance, allowing you to experience every moment of a piece of porcelain from kneading clay, throwing, to painting.',
      process: 'Experience Process',
      recommended: 'Recommended Workshops',
      qualificationApproved: 'Qualification Approved',
      viewDetail: 'View Details',
      experience: 'Experience Projects',
      price: 'Price',
      duration: 'Duration',
      location: 'Location',
      phone: 'Phone',
      openTime: 'Opening Hours',
      description: 'Workshop Introduction',
      experienceDesc: 'Experience Description',
      experienceDescText: 'This workshop provides various ceramic experience projects such as throwing, painting, and firing. Specific experience content and charging standards are subject to on-site conditions. You can call in advance for consultation or visit the store to learn more.',
      noDescription: 'This workshop has not yet completed a detailed introduction.',
      noInfo: 'No Information',
      notFound: 'Ceramic workshop information not found',
      description: 'Quality ceramic workshop providing professional ceramic experience services for you.',
      booked: 'Booked',
      bookNow: 'Book Now',
      collapseReservation: 'Collapse',
      reservation: 'Reservation',
      name: 'Name',
      phone: 'Phone',
      participants: 'Participants',
      date: 'Date',
      time: 'Time',
      timeSlot: 'Time Slot',
      note: 'Notes',
      processStep1: 'Throwing',
      features: {
        professional: {
          title: 'Professional Guidance',
          desc: 'Experienced ceramic artists provide one-on-one guidance to ensure you can successfully complete your work'
        },
        tools: {
          title: 'Complete Tools',
          desc: 'Professional ceramic tools and equipment provided, no need to bring any materials'
        },
        firing: {
          title: 'Firing Service',
          desc: 'Professional kiln firing ensures quality and aesthetics of your work'
        },
        creative: {
          title: 'Unlimited Creativity',
          desc: 'Freely express your creativity and create unique ceramic works'
        }
      },
      process: {
        step1: {
          title: 'Choose Shape',
          desc: 'Choose from cups, teapots, vases and other shapes'
        },
        step2: {
          title: 'Throwing',
          desc: 'Complete throwing on the wheel, slowly shaping your own form'
        },
        step3: {
          title: 'Decoration & Painting',
          desc: 'Add details and colors to your work using carving, stamping, and painting techniques'
        },
        step4: {
          title: 'Firing Complete',
          desc: 'After high-temperature firing, wait for your work to be ready and delivered to your home'
        }
      },
      nameRequired: 'Please enter your name',
      phoneRequired: 'Please enter phone number',
      phoneInvalid: 'Please enter a valid phone number',
      dateRequired: 'Please select reservation date',
      timeRequired: 'Please select time slot'
    }
  },
  plan: {
    title: 'Trip Planning',
    subtitle: 'Your intelligent travel assistant, customizing the perfect journey for you',
    currentView: 'Current View:',
    selectPlan: 'Select Plan',
    createNew: 'Create New Plan',
    firstPlan: 'Start Your First Trip Planning',
    cancel: 'Cancel',
    days: 'Travel Days',
    selectDays: 'Select Days',
    startDate: 'Departure Date',
    selectDate: 'Select Date',
    budget: 'Budget Range',
    selectBudget: 'Select Budget',
    budgetLow: 'Economy (Under ¥500)',
    budgetMedium: 'Comfort (¥500-1000)',
    budgetHigh: 'Luxury (Over ¥1000)',
    interests: 'Interests',
    ceramicCulture: 'Ceramic Culture',
    naturalScenery: 'Natural Scenery',
    historicalSites: 'Historical Sites',
    modernArt: 'Modern Art',
    transport: 'Transportation',
    walking: 'Walking',
    driving: 'Driving',
    public: 'Public Transport',
    generate: 'Generate Intelligent Plan',
    reset: 'Reset',
    myPlan: 'My Plan',
    inProgress: 'In Progress',
    daysLabel: 'Days',
    budgetLabel: 'Budget',
    startDateLabel: 'Departure Date',
    interestsLabel: 'Interests',
    transportLabel: 'Transportation',
    day: 'days',
    dayTitle: 'Day {day} Itinerary',
    emptySchedule: 'No Schedule',
    edit: 'Edit',
    delete: 'Delete',
    save: 'Save',
    editTitle: 'Edit Plan',
    editDescription: 'Edit Plan Description',
    generating: 'Generating plan...',
    generateFailed: 'Failed to generate plan',
    noPlans: 'No plans yet',
    loadFailed: 'Failed to load plans',
    tips: 'Travel Tips',
    tipsContent: 'Ceramic marketplaces in Jingdezhen are usually open on weekends. It is recommended to plan your time in advance!',
    theme: 'Theme',
    addActivity: 'Add Activity',
    time: 'Time',
    activityTitle: 'Activity Title',
    activityDesc: 'Activity Description/Notes',
    emptyDaySchedule: 'No schedule for this day'
  },
  recommend: {
    title: 'Smart Recommendation',
    subtitle: 'Based on your preferences, recommend the most suitable content for you',
    recommendContent: 'Content Type',
    recommendMethod: 'Method',
    filterCondition: 'Filters',
    reset: 'Reset',
    category: 'Category',
    all: 'All',
    price: 'Price',
    rating: 'Rating',
    recommendResults: 'Results',
    totalItems: '{total} items found',
    noRecommend: 'No recommendations found, please adjust filters',
    location: {
      requesting: 'Locating...',
      success: 'Located',
      error: 'Failed',
      click: 'Locate'
    },
    sort: 'Sort: ',
    type: {
      scenic: 'Scenic',
      food: 'Food',
      route: 'Route',
      hotel: 'Hotel'
    },
    method: {
      itemBased: 'Item-based',
      userBased: 'User-based',
      contentBased: 'Content-based'
    },
    algo: {
      hot: 'Popular',
      personal: 'For You',
      similar: 'Similar',
      nearby: 'Nearby'
    },
    filter: {
      priceHigh: 'Price: High to Low',
      priceLow: 'Price: Low to High',
      ratingHigh: 'Rating: High to Low',
      ratingLow: 'Rating: Low to High'
    },
    priceRange: {
      free: 'Free',
      below50: 'Under ¥50',
      range50_100: '¥50-100',
      above100: 'Over ¥100'
    },
    ratingRange: {
      above4_5: 'Above 4.5',
      above4_0: 'Above 4.0',
      above3_5: 'Above 3.5'
    },
    sortOptions: {
      recommend: 'Recommended',
      rating: 'Rating',
      price: 'Price',
      popularity: 'Popularity'
    },
    categories: {
      scenic: {
        cultural: 'Cultural Scenic Area',
        museum: 'Museum',
        ancientTown: 'Ancient Town',
        architecture: 'Ancient Architecture',
        miningPark: 'Mining Park',
        ceramicVillage: 'Ceramic Village'
      },
      food: {
        snack: 'Snack',
        chinese: 'Chinese Food',
        western: 'Western Food',
        dessert: 'Dessert',
        drink: 'Drink',
        specialty: 'Specialty'
      },
      hotel: {
        starHotel: 'Star Hotel',
        homestay: 'Homestay',
        inn: 'Inn',
        youthHostel: 'Youth Hostel',
        businessHotel: 'Business Hotel'
      },
      route: {
        oneDay: '1-Day Tour',
        twoDays: '2-Day Tour',
        threeDays: '3-Day Tour',
        deep: 'Deep Tour',
        family: 'Family Tour',
        cultural: 'Cultural Tour'
      }
    },
    unit: {
      person: 'person',
      night: 'night'
    },
    route: {
      days: '{days}-Day Tour',
      containsSpots: 'Includes ({count} spots)',
      moreSpots: '+{count} more',
      usePlan: 'Use for Planning'
    }
  },
  home: {
    hero: {
      title: 'Explore the Porcelain Capital',
      subtitle: 'Millennium Jingdezhen, Expect Every Encounter',
      searchPlaceholder: 'Search Jingdezhen attractions / food / routes / hotels...'
    },
    access: {
      scenic: 'Scenery',
      scenicDesc: 'Popular Spots',
      food: 'Food',
      foodDesc: 'Local Flavor',
      route: 'Routes',
      routeDesc: 'Selected Trips',
      hotel: 'Hotels',
      hotelDesc: 'Comfort Stay',
      ceramic: 'Ceramic',
      ceramicDesc: 'DIY Fun'
    },
    sidebar: {
      top: 'Top',
      profile: 'Profile',
      plan: 'My Plan'
    },
    section: {
      popularScenic: 'Popular Scenic Spots',
      popularScenicSub: 'POPULAR SCENIC SPOTS',
      ceramic: 'Ceramic Experience',
      ceramicSub: 'CERAMIC EXPERIENCE',
      recommend: 'Recommended For You',
      recommendSub: 'RECOMMENDED FOR YOU',
      searchResult: 'Search: {keyword}'
    },
    action: {
      viewMore: 'View More',
      clearSearch: 'Clear Search'
    },
    status: {
      noScenic: 'No scenic spots found',
      noCeramic: 'No ceramic experiences found'
    },
    message: {
      inputKeyword: 'Please enter a keyword',
      noResult: 'No related merchants found',
      foundResult: 'Found {count} related merchants',
      searchFailed: 'Search failed, please try again',
      loadFailed: 'Failed to load data'
    },
    ceramicData: {
      workshop: {
        title: 'Pottery DIY Workshop',
        desc: 'Handmade ceramic works, experience the fun of traditional porcelain making'
      },
      market: {
        title: 'Ceramic Market',
        desc: 'Experience modern ceramic art in Taoxichuan Cultural and Creative Block'
      },
      kiln: {
        title: 'Ancient Kiln Experience',
        desc: 'Experience traditional porcelain making in the Ancient Kiln Folk Custom Expo Area'
      }
    }
  },
  tag: {
    '5A景区': '5A Scenic Spot',
    '4A景区': '4A Scenic Spot',
    '文化遗产': 'Cultural Heritage',
    '打卡圣地': 'Must-visit',
    '免费': 'Free',
    '历史悠久': 'Historical',
    '陶瓷文化': 'Ceramic Culture',
    '自然风光': 'Nature',
    '亲子游': 'Family Friendly',
    '博物馆': 'Museum',
    '艺术': 'Art',
    '购物': 'Shopping',
    '网红': 'Popular'
  },
  role: {
    tourist: 'Tourist',
    merchant: 'Merchant',
    admin: 'Admin',
    user: 'User',
    guest: 'Guest'
  },
  profile: {
    menu: {
      info: 'Profile Info',
      favorites: 'My Favorites',
      reviews: 'My Reviews',
      orders: 'My Orders',
      plans: 'My Plans',
      settings: 'Settings'
    },
    info: {
      title: 'Profile Info',
      username: 'Username',
      phone: 'Phone',
      email: 'Email',
      bio: 'Bio',
      registerTime: 'Registered',
      notSet: 'Not Set',
      defaultBio: 'This user is lazy and left nothing...'
    },
    placeholder: {
      username: 'Enter username',
      phone: 'Enter phone number',
      email: 'Enter email',
      bio: 'Enter bio'
    },
    validation: {
      usernameRequired: 'Username is required',
      phoneInvalid: 'Invalid phone number',
      emailInvalid: 'Invalid email format',
      fillAll: 'Please fill in all required fields',
      passwordMismatch: 'Passwords do not match',
      passwordLength: 'Password must be at least 6 characters'
    },
    favorites: {
      title: 'My Favorites',
      empty: 'No favorites yet'
    },
    reviews: {
      title: 'My Reviews',
      unknownScenic: 'Unknown Scenic Spot',
      empty: 'No reviews yet'
    },
    orders: {
      title: 'My Orders',
      ceramicTitle: 'Ceramic Workshop Bookings (Local)',
      workshopName: 'Workshop Name',
      price: 'Price',
      duration: 'Duration',
      timeSlot: 'Time Slot',
      bookingTime: 'Booking Time',
      noCeramicBooking: 'No ceramic workshop bookings',
      orderId: 'Order ID',
      name: 'Name',
      unknownOrder: 'Unknown Order',
      totalPrice: 'Total Price',
      status: 'Status',
      orderTime: 'Order Time',
      empty: 'No orders yet'
    },
    orderStatus: {
      pending: 'Pending',
      confirmed: 'Confirmed',
      cancelled: 'Cancelled'
    },
    plans: {
      title: 'My Plans',
      create: 'Create New Plan',
      daysSuffix: ' Days',
      createTime: 'Created At',
      estimatedCost: 'Est. Cost',
      empty: 'No plans yet'
    },
    settings: {
      title: 'Account Settings',
      changePassword: 'Change Password',
      notification: 'Notification Settings',
      emailNotification: 'Email Notification',
      smsNotification: 'SMS Notification',
      privacy: 'Privacy Settings',
      publicProfile: 'Public Profile',
      save: 'Save Settings'
    },
    password: {
      old: 'Old Password',
      new: 'New Password',
      confirm: 'Confirm Password'
    },
    planDetail: {
      title: 'Plan Details',
      startDate: 'Start Date',
      duration: 'Duration'
    },
    editPlan: {
      title: 'Edit Plan',
      labelTitle: 'Plan Title',
      labelDescription: 'Plan Description',
      labelDetails: 'Plan Details (JSON)'
    },
    orderDetail: {
      title: 'Order Details',
      status: 'Order Status',
      merchant: 'Merchant Name',
      amount: 'Order Amount',
      roomType: 'Room Info',
      roomQuantity: 'Room Quantity',
      roomUnit: 'Rooms',
      checkInTime: 'Check-in Time',
      note: 'Note',
      createTime: 'Create Time'
    },
    message: {
      checkForm: 'Please check the form',
      saveInfoFailed: 'Failed to save user info',
      imageOnly: 'Only image files are allowed!',
      imageSizeLimit: 'Image size cannot exceed 5MB!',
      avatarUploaded: 'Avatar uploaded successfully',
      avatarUpdateFailed: 'Failed to update avatar',
      avatarUploadRetry: 'Avatar upload failed, please try again',
      deleteSuccess: 'Deleted successfully',
      deleteFailed: 'Failed to delete',
      orderCancelled: 'Order cancelled',
      cancelFailed: 'Failed to cancel order',
      loadPlanDetailFailed: 'Failed to load plan details',
      planUpdated: 'Plan updated successfully',
      planUpdateFailed: 'Failed to update plan',
      confirmDeletePlan: 'Are you sure you want to delete this plan?',
      planDeleteFailed: 'Failed to delete plan',
      settingsSaved: 'Settings saved',
      settingsSaveFailed: 'Failed to save settings',
      passwordChanged: 'Password changed successfully',
      passwordChangeFailed: 'Failed to change password',
      loadReviewFailed: 'Failed to load reviews',
      orderConfirmed: 'Order #{id} has been confirmed by merchant'
    },
    action: {
      view: 'View',
      close: 'Close'
    }
  }
};

