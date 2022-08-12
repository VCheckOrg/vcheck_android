package com.vcheck.sdk.core.presentation.screens

//class SeparatedSuccessFragment : ThemeWrapperFragment() {
//
//    private var _binding: FragmentSeparatedSuccessBinding? = null
//
//    override fun changeColorsToCustomIfPresent() {
//        VCheckSDK.buttonsColorHex?.let {
//            _binding!!.successButton.setBackgroundColor(Color.parseColor(it))
//        }
//        VCheckSDK.backgroundPrimaryColorHex?.let {
//            _binding!!.backgroundHolder.background = ColorDrawable(Color.parseColor(it))
//        }
//        VCheckSDK.backgroundSecondaryColorHex?.let {
//            _binding!!.card.setCardBackgroundColor(Color.parseColor(it))
//        }
//        VCheckSDK.primaryTextColorHex?.let {
//            _binding!!.successTitle.setTextColor(Color.parseColor(it))
//            _binding!!.successButton.setTextColor(Color.parseColor(it))
//        }
//        VCheckSDK.secondaryTextColorHex?.let {
//            _binding!!.inProcessSubtitle.setTextColor(Color.parseColor(it))
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?, savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_separated_success, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        _binding = FragmentSeparatedSuccessBinding.bind(view)
//
//        changeColorsToCustomIfPresent()
//
//        _binding!!.successButton.setOnClickListener {
//            VCheckSDK.onFinish()
//        }
//    }
//}